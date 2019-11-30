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

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.FeaturePlugin;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.*;
import org.exoplatform.smartactivitystream.stats.settings.GlobalSettings;
import org.exoplatform.smartactivitystream.stats.ActivityStatsService;
import org.exoplatform.smartactivitystream.stats.utils.StatsUtils;

/**
 * A plugin added to {@link ExoFeatureService} that determines if 'stats' is
 * enabled for a user or not
 */
public class StatsFeaturePlugin extends FeaturePlugin {

  private static final String STATS_FEATURE_NAME = "stats";

  private ConversationRegistry conversationRegistry;

  private ActivityStatsService activityStatsService;

  @Override
  public String getName() {
    return STATS_FEATURE_NAME;
  }

  @Override
  public boolean isFeatureActiveForUser(String featureName, String username) {
    List<StateKey> stateKeys = getConversationRegistry().getStateKeys(username);
    for (StateKey stateKey : stateKeys) {
      ConversationState state = getConversationRegistry().getState(stateKey);
      Boolean statsEnabled = (Boolean) state.getAttribute("stats.enabled");
      if (statsEnabled != null) {
        return statsEnabled;
      }
    }
    GlobalSettings settings = getActivityStatsService().getSettings();
    if (settings == null) {
      return false;
    }
    String accessPermission = settings.getAccessPermission();
    if (StringUtils.isBlank(accessPermission)) {
      return true;
    }
    boolean statsEnabled = StatsUtils.isUserMemberOfSpaceOrGroupOrUser(username, accessPermission);
    for (StateKey stateKey : stateKeys) {
      ConversationState state = getConversationRegistry().getState(stateKey);
      state.setAttribute("stats.enabled", statsEnabled);
    }
    return statsEnabled;
  }

  /**
   * The Service can't be injected by constructor to avoid cyclic dependency
   *
   * @return instance of {@link ConversationRegistry} injected in current
   *         container
   */
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
  private ActivityStatsService getActivityStatsService() {
    if (activityStatsService == null) {
      activityStatsService = CommonsUtils.getService(ActivityStatsService.class);
    }
    return activityStatsService;
  }
}
