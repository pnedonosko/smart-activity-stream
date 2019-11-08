/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.smartactivitystream.stats.domain;

import java.io.Serializable;

/**
 * The Class ActivityFocusId.
 */

public class ActivityFocusId implements Serializable {

  /** The user id. */
  private String userId;

  /** The activity id. */
  private String activityId;

  /** The start time. */
  private Long   startTime;

  /**
   * Instantiates a new ActivityFocusId.
   */
  public ActivityFocusId() {
  }

  /**
   * Instantiates a new ActivityFocusId.
   *
   * @param userId the user id
   * @param activityId the activity id
   * @param startTime the start time
   */
  public ActivityFocusId(String userId, String activityId, long startTime) {
    setUserId(userId);
    setActivityId(activityId);
    setStartTime(startTime);
  }

  /**
   * Gets the user id.
   *
   * @return user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the user id.
   *
   * @param userId the user id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Gets the activity id.
   *
   * @return user id
   */
  public String getActivityId() {
    return activityId;
  }

  /**
   * Sets the activity id.
   *
   * @param activityId the activity id
   */
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  /**
   * Gets the start time.
   *
   * @return user id
   */
  public Long getStartTime() {
    return startTime;
  }

  /**
   * Sets the start time.
   *
   * @param startTime the start time
   */
  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int hc = 7 + getUserId().hashCode();
    hc = hc * 31 + getActivityId().hashCode();
    hc = hc * 31 + (int) (getStartTime() ^ (getStartTime() >>> 32));
    return hc;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (o != null) {
      if (ActivityFocusId.class.isAssignableFrom(o.getClass())) {
        ActivityFocusId other = ActivityFocusId.class.cast(o);
        return getUserId().equals(other.getUserId()) && getActivityId().equals(other.getActivityId())
            && getStartTime() == other.getStartTime();
      }
    }
    return false;
  }

}
