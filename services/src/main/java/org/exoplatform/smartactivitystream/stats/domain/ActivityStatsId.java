package org.exoplatform.smartactivitystream.stats.domain;

import javax.persistence.IdClass;
import java.io.Serializable;

/**
 * The Class ActivityStatsId.
 */

@IdClass(ActivityStatsId.class)
public class ActivityStatsId implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -3465780665489127013L;

  /** The activity id. */
  protected String          activityId;

  /** The start time. */
  protected long            startTime;

  /**
   * Instantiates a new ActivityFocusId.
   */
  public ActivityStatsId() {
  }

  /**
   * Instantiates a new ActivityStatsId.
   *
   * @param activityId the activity id
   * @param startTime the start time
   */
  public ActivityStatsId(String activityId, long startTime) {
    super();
    this.activityId = activityId;
    this.startTime = startTime;
  }

  /**
   * Gets the activity id.
   *
   * @return activity id
   */
  public String getActivityId() {
    return activityId;
  }

  /**
   * Gets the start time.
   *
   * @return the startTime
   */
  public long getStartTime() {
    return startTime;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int hc = activityId.hashCode();
    hc = hc * 31 + (int) (startTime ^ (startTime >>> 32));
    return hc;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (o != null) {
      if (ActivityStatsId.class.isAssignableFrom(o.getClass())) {
        ActivityStatsId other = ActivityStatsId.class.cast(o);
        return activityId.equals(other.getActivityId()) && startTime == other.getStartTime();
      }
    }
    return false;
  }
}
