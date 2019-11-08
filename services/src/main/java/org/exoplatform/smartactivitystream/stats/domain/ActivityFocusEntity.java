/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko & Nick
 *         Riabovol</a>
 * @version $Id: ActivityFocusEntity.java 00000 Nov 5, 2019 pnedonosko $
 */
@Entity(name = "SmartActivityFocus")
@ExoEntity
@Table(name = "ST_ACTIVITY_FOCUS")
@IdClass(ActivityFocusId.class)
@NamedQueries({
    @NamedQuery(name = "SmartActivityFocus.findTotalCount", query = "SELECT COUNT(f.activityId) FROM SmartActivityFocus f"),
    @NamedQuery(name = "SmartActivityFocus.findAllFocus", query = "SELECT f FROM SmartActivityFocus f "
        + "WHERE f.userId = :userId AND f.activityId = :activityId ORDER BY f.startTime ASC"),
    @NamedQuery(name = "SmartActivityFocus.findFocusAfter", query = "SELECT f FROM SmartActivityFocus f "
        + "WHERE f.userId = :userId AND f.activityId = :activityId AND f.startTime >= :startTimeAfter ORDER BY f.startTime DESC") })

public class ActivityFocusEntity extends BaseActivityFocusEntity {

  private static final Long  NULL_LONG       = new Long(-1);

  /** The Constant TRACKER_VERSION. */
  public static final String TRACKER_VERSION = "1.0";

  /** The user id. */
  @Id
  @Column(name = "USER_ID", nullable = false)
  private String             userId;

  /** The activity id. */
  @Id
  @Column(name = "ACTIVITY_ID", nullable = false)
  private String           activityId;

  /** The start time. */
  @Id
  @Column(name = "START_TIME", nullable = false)
  private Long             startTime;

  /** The tracker version. */
  @Column(name = "TRACKER_VERSION", nullable = false)
  private String           trackerVersion;

  /** The hash code. */
  private transient int      hashCode;

  /**
   * Instantiates a new activity focus.
   */
  public ActivityFocusEntity() {
  }

  /*public ActivityFocusEntity(String userId,
                             String activityId,
                             Long startTime,
                             Long stopTime,
                             Long totalShown,
                             Long contentShown,
                             Long convoShown,
                             Long contentHits,
                             Long convoHits,
                             Long appHits,
                             Long profileHits,
                             Long linkHits,
                             String trackerVersion) {
    super(stopTime, totalShown, contentShown, convoShown, contentHits, convoHits, appHits, profileHits, linkHits);
    setActivityId(activityId);
    setStartTime(startTime);
    setUserId(userId);
    setTrackerVersion(trackerVersion);
    setHashCode(0);
  }*/

  /**
   * Gets the user id.
   *
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the user id.
   *
   * @param userId the userId to set
   */
  public void setUserId(String userId) {
    this.userId = userId;
    setHashCode(0);
  }

  /**
   * Gets the activity id.
   *
   * @return the activityId
   */
  public String getActivityId() {
    return activityId;
  }

  /**
   * Sets the activity id.
   *
   * @param activityId the activityId to set
   */
  public void setActivityId(String activityId) {
    this.activityId = activityId;
    setHashCode(0);
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
   * @param startTime the startTime to set
   */
  public void setStartTime(Long startTime) {
    this.startTime = startTime;
    setHashCode(0);
  }

  /**
   * Gets the tracker version.
   *
   * @return the trackerVersion
   */
  public String getTrackerVersion() {
    return trackerVersion;
  }

  /**
   * Sets the tracker version.
   *
   * @param trackerVersion the trackerVersion to set
   */
  public void setTrackerVersion(String trackerVersion) {
    this.trackerVersion = trackerVersion;
    setHashCode(0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);

    // Always have value
    out.writeUTF(getActivityId());
    out.writeLong(getStartTime());
    out.writeUTF(getUserId());
    out.writeUTF(getTrackerVersion());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    setHashCode(0);
    super.readExternal(in);

    // Always have value
    setActivityId(in.readUTF());
    setStartTime(in.readLong());
    setUserId(in.readUTF());
    setTrackerVersion(in.readUTF());
  }

  @Override
  public int getHashCode() {
    return hashCode;
  }

  private void setHashCode(int hashCode) {
    this.hashCode = hashCode;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    if (getHashCode() == 0) {
      final int prime = 31;
      int result = 1;
      result = prime * result + super.hashCode();
      result = prime * result + ((getActivityId() == null) ? 0 : getActivityId().hashCode());
      result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
      result = prime * result + ((getTrackerVersion() == null) ? 0 : getTrackerVersion().hashCode());
      result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
      setHashCode(result);
    }
    return getHashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj != null && ActivityFocusEntity.class.isAssignableFrom(obj.getClass())) {
      ActivityFocusEntity other = ActivityFocusEntity.class.cast(obj);
      if (!super.equals(obj)) {
        return false;
      }
      if (getActivityId() == null) {
        if (other.getActivityId() != null)
          return false;
      } else if (!getActivityId().equals(other.getActivityId()))
        return false;
      if (getStartTime() == null) {
        if (other.getStartTime() != null)
          return false;
      } else if (!getStartTime().equals(other.getStartTime()))
        return false;
      if (getTrackerVersion() == null) {
        if (other.getTrackerVersion() != null)
          return false;
      } else if (!getTrackerVersion().equals(other.getTrackerVersion()))
        return false;
      if (getUserId() == null) {
        if (other.getUserId() != null)
          return false;
      } else if (!getUserId().equals(other.getUserId()))
        return false;
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append(this.getClass().getSimpleName());
    s.append('-');
    s.append(getUserId());
    s.append('@');
    s.append(getActivityId());
    s.append('-');
    s.append(getStartTime());
    return s.toString();
  }

}
