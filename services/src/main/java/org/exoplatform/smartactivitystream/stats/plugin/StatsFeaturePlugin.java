/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.smartactivitystream.stats.plugin;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.FeaturePlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.smartactivitystream.stats.ActivityStatsService;

/**
 * A plugin added to {@link ExoFeatureService} that determines if 'stats' is
 * enabled for a user or not
 */
public class StatsFeaturePlugin extends FeaturePlugin {

  /** The Constant LOG. */
  private static final Log           LOG                = ExoLogger.getLogger(StatsFeaturePlugin.class);

  private static final String        STATS_FEATURE_NAME = "stats";

  /** The identity registry. */
  private final IdentityRegistry     identityRegistry;

  /** The authenticator. */
  private final Authenticator        authenticator;

  private final ActivityStatsService activityStatsService;

  public StatsFeaturePlugin(ActivityStatsService activityStatsService,
                            IdentityRegistry identityRegistry,
                            Authenticator authenticator) {
    this.activityStatsService = activityStatsService;
    this.identityRegistry = identityRegistry;
    this.authenticator = authenticator;
  }

  @Override
  public String getName() {
    return STATS_FEATURE_NAME;
  }

  @Override
  public boolean isFeatureActiveForUser(String featureName, String username) {

    String allowedIdentities = activityStatsService.getAllowedIdentities();
    if (StringUtils.isBlank(allowedIdentities)) {
      return true;
    }

    return isUserMemberOfGroupOrUser(username, allowedIdentities);
  }

  private final boolean isUserMemberOfGroupOrUser(String username, String allowedIdentities) {
    if (StringUtils.isBlank(allowedIdentities)) {
      throw new IllegalArgumentException("AllowedIdentities expression is mandatory");
    }
    if (StringUtils.isBlank(username)) {
      return false;
    }

    org.exoplatform.services.security.Identity identity = identityRegistry.getIdentity(username);
    if (identity == null) {
      try {
        identity = authenticator.createIdentity(username);
      } catch (Exception e) {
        LOG.warn("Error getting memberships of user {}", username, e);
      }
    }
    if (identity == null) {
      return false;
    }

    MembershipEntry membership = null;
    boolean isMember = false;
    boolean isAllowedUser = false;

    String[] allowedIdentitiesParts = allowedIdentities.split(" ");
    for (String allowedIdentity : allowedIdentitiesParts) {
      if (allowedIdentity.contains(":")) {
        String[] permissionExpressionParts = allowedIdentity.split(":");
        membership = new MembershipEntry(permissionExpressionParts[1], permissionExpressionParts[0]);
      } else if (allowedIdentity.contains("/")) {
        membership = new MembershipEntry(allowedIdentity, MembershipEntry.ANY_TYPE);
      } else {
        isAllowedUser = StringUtils.equals(username, allowedIdentity);

        if (isAllowedUser) {
          return isAllowedUser;
        }
      }

      isMember = identity.isMemberOf(membership);

      if (isMember) {
        return isMember;
      }
    }

    return isMember;
  }

}
