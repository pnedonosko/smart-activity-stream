package org.exoplatform.smartactivitystream.stats.utils;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.*;
import org.exoplatform.social.core.space.spi.SpaceService;

public class StatsUtils {

  /** The Constant LOG. */
  private static final Log LOG = ExoLogger.getLogger(StatsUtils.class);

  public static final boolean isUserMemberOfSpaceOrGroupOrUser(String username, String accessPermission) {
    boolean isMember = true;
    if (StringUtils.isNotBlank(accessPermission)) {
      Space space = getSpace(accessPermission);
      isMember = (space != null && getSpaceService().isMember(space, username))
          || isUserMemberOfGroupOrUser(username, accessPermission);
    }
    return isMember;
  }

  public static final boolean isUserMemberOfGroupOrUser(String username, String permissionExpression) {
    if (StringUtils.isBlank(permissionExpression)) {
      throw new IllegalArgumentException("Permission expression is mandatory");
    }
    if (StringUtils.isBlank(username)) {
      return false;
    }

    org.exoplatform.services.security.Identity identity = CommonsUtils.getService(IdentityRegistry.class).getIdentity(username);
    if (identity == null) {
      try {
        identity = CommonsUtils.getService(Authenticator.class).createIdentity(username);
      } catch (Exception e) {
        LOG.warn("Error getting memberships of user {}", username, e);
      }
    }
    if (identity == null) {
      return false;
    }
    MembershipEntry membership = null;
    if (permissionExpression.contains(":")) {
      String[] permissionExpressionParts = permissionExpression.split(":");
      membership = new MembershipEntry(permissionExpressionParts[1], permissionExpressionParts[0]);
    } else if (permissionExpression.contains("/")) {
      membership = new MembershipEntry(permissionExpression, MembershipEntry.ANY_TYPE);
    } else {
      return StringUtils.equals(username, permissionExpression);
    }
    return identity.isMemberOf(membership);
  }

  public static Space getSpace(String id) {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    if (id.indexOf("/spaces/") >= 0) {
      return spaceService.getSpaceByGroupId(id);
    }
    Space space = spaceService.getSpaceByPrettyName(id);
    if (space == null) {
      space = spaceService.getSpaceByGroupId("/spaces/" + id);
      if (space == null) {
        space = spaceService.getSpaceByDisplayName(id);
        if (space == null) {
          space = spaceService.getSpaceByUrl(id);
          if (space == null) {
            space = spaceService.getSpaceById(id);
          }
        }
      }
    }
    return space;
  }

  private static final SpaceService getSpaceService() {
    return CommonsUtils.getService(SpaceService.class);
  }
}
