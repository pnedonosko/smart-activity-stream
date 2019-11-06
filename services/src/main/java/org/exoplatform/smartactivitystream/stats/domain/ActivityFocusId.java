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

import javax.persistence.IdClass;
import java.io.Serializable;

/**
 * The Class ActivityFocusId.
 */

@IdClass(ActivityFocusId.class)
public class ActivityFocusId extends ActivityStatsId {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -3465780665489127013L;

  /** The user id. */
  protected String          userId;

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
    super(activityId, startTime);
    this.userId = userId;
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
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int hc = 7 + userId.hashCode();
    hc = hc * 31 + this.getActivityId().hashCode();
    hc = hc * 31 + (int) (this.getStartTime() ^ (this.getStartTime() >>> 32));
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
        return userId.equals(other.getUserId()) && super.equals(o);
      }
    }
    return false;
  }

}
