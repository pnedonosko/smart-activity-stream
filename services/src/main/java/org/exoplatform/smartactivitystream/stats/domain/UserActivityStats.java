package org.exoplatform.smartactivitystream.stats.domain;

import java.util.List;
import java.util.Objects;

/**
 * The Class UserActivityStats.
 */

public class UserActivityStats {

  /** Activity stats entities for the main table. */
  private final List<ActivityStatsEntity> activityStatsEntities;

  /** The activity statistics max total shown value (for the general scaling of the main table charts). */
  private final Long                      maxTotalShown;

  public UserActivityStats(List<ActivityStatsEntity> activityStatsEntities, Long maxTotalShown) {
    this.activityStatsEntities = activityStatsEntities;
    this.maxTotalShown = maxTotalShown;
  }

  public List<ActivityStatsEntity> getActivityStatsEntities() {
    return activityStatsEntities;
  }

  public Long getMaxTotalShown() {
    return maxTotalShown;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    UserActivityStats that = (UserActivityStats) o;
    return Objects.equals(activityStatsEntities, that.activityStatsEntities) && Objects.equals(maxTotalShown, that.maxTotalShown);
  }

  @Override
  public int hashCode() {
    return Objects.hash(activityStatsEntities, maxTotalShown);
  }
}
