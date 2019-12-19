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
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.*;
import org.exoplatform.smartactivitystream.stats.ActivityStatsService;
import org.exoplatform.smartactivitystream.stats.utils.StatsUtils;

/**
 * A plugin added to {@link ExoFeatureService} that determines if 'stats' is
 * enabled for a user or not
 */
public class StatsFeaturePlugin extends FeaturePlugin {

  private static final String  STATS_FEATURE_NAME = "stats";

  // TODO should be final
  private ConversationRegistry conversationRegistry;
  // TODO should be final
  private ActivityStatsService activityStatsService;

  @Override
  public String getName() {
    return STATS_FEATURE_NAME;
  }

  @Override
  public boolean isFeatureActiveForUser(String featureName, String username) {

    ActivityStatsService activityStatsService = getActivityStatsService();
    // TODO Why we need this check as such?
    if (!activityStatsService.isServiceEnabled()) {
      return false;
    }

    String accessPermission = activityStatsService.getAccessPermission();
    if (StringUtils.isBlank(accessPermission)) {
      return true;
    }

    return StatsUtils.isUserMemberOfSpaceOrGroupOrUser(username, accessPermission);
  }

  /**
   * The Service can't be injected by constructor to avoid cyclic dependency
   *
   * @return instance of {@link ConversationRegistry} injected in current
   *         container
   */
  @Deprecated // TODO it's plugin dependency - inject via container
  private ConversationRegistry getConversationRegistry() {
    if (conversationRegistry == null) {
      conversationRegistry = CommonsUtils.getService(ConversationRegistry.class);
    }
    return conversationRegistry;
  }

  /**
   * The Service can't be injected by constructor to avoid cyclic dependency
   *
   * @return instance of {@link ActivityStatsService} injected in current
   *         container
   */
  @Deprecated // TODO it's plugin dependency - inject via container
  private ActivityStatsService getActivityStatsService() {
    if (activityStatsService == null) {
      activityStatsService = CommonsUtils.getService(ActivityStatsService.class);
    }
    return activityStatsService;
  }
}
