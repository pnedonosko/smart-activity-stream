package org.exoplatform.smartactivitystream.stats.domain;

import java.io.Serializable;

/**
 * The Class ActivityStatsId.
 */

public class ActivityStatsId implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -3465780665489127013L;

  /** The activity id. */
  protected String          activityId;

  /** The start time. */
  protected long            startTime;

  /**
   * Instantiates a new ActivityStatsId.
   */
  public ActivityStatsId() {
  }

  /**
   * Instantiates a new ActivityStatsId.
   *
   * @param activityId the activity id
   * @param startTime the start time
   */
  public ActivityStatsId(String activityId, Long startTime) {
    super();
    setActivityId(activityId);
    setStartTime(startTime);
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
   * Sets the activity id.
   *
   * @param activityId the activity id
   */
  private void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  /**
   * Gets the start time.
   *
   * @return the startTime
   */
  public Long getStartTime() {
    return startTime;
  }

  /**
   * Sets the start time.
   *
   * @param startTime the start time
   */
  private void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int hc = getActivityId().hashCode();
    hc = hc * 31 + (int) (getStartTime() ^ (getStartTime() >>> 32));
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
        return getActivityId().equals(other.getActivityId()) && getStartTime() == other.getStartTime();
      }
    }
    return false;
  }
}
