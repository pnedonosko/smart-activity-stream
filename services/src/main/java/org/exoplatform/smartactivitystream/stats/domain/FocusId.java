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
 * The Class FocusId.
 */
public class FocusId implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -3464440639182127013L;

  /** The user id. */
  protected String          userId;

  /** The activity id. */
  protected String          activityId;

  /** The start time. */
  protected long            startTime;

  /**
   * Instantiates a new FocusId.
   */
  public FocusId() {
  }

  /**
   * Instantiates a new FocusId.
   *
   * @param userId the user id
   * @param activityId the activity id
   * @param startTime the start time
   */
  public FocusId(String userId, String activityId, long startTime) {
    super();
    this.userId = userId;
    this.activityId = activityId;
    this.startTime = startTime;
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
    int hc = 7 + userId.hashCode();
    hc = hc * 31 + activityId.hashCode();
    hc = hc * 31 + (int) (startTime ^ (startTime >>> 32));
    return hc;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (o != null) {
      if (FocusId.class.isAssignableFrom(o.getClass())) {
        FocusId other = FocusId.class.cast(o);
        return userId.equals(other.getUserId()) && activityId.equals(other.getActivityId()) && startTime == other.startTime;
      }
    }
    return false;
  }

}
