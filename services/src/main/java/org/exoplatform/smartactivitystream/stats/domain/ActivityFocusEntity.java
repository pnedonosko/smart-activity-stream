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
  protected String           activityId;

  /** The start time. */
  @Id
  @Column(name = "START_TIME", nullable = false)
  protected Long             startTime;

  /** The tracker version. */
  @Column(name = "TRACKER_VERSION", nullable = false)
  protected String           trackerVersion;

  /** The hash code. */
  private transient int      hashCode;

  /**
   * Instantiates a new activity focus.
   */
  public ActivityFocusEntity() {
  }

  public ActivityFocusEntity(String userId,
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
    this.activityId = activityId;
    this.startTime = startTime;
    this.userId = userId;
    this.trackerVersion = trackerVersion;
    this.hashCode = 0;
  }

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
    this.hashCode = 0;
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
    this.hashCode = 0;
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
    this.hashCode = 0;
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
    this.hashCode = 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);

    // Always have value
    out.writeUTF(activityId);
    out.writeLong(startTime);
    out.writeUTF(userId);
    out.writeUTF(trackerVersion);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    hashCode = 0;
    super.readExternal(in);

    // Always have value
    activityId = in.readUTF();
    startTime = in.readLong();
    userId = in.readUTF();
    trackerVersion = in.readUTF();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    if (hashCode == 0) {
      final int prime = 31;
      int result = 1;
      result = prime * result + super.hashCode();
      result = prime * result + ((activityId == null) ? 0 : activityId.hashCode());
      result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
      result = prime * result + ((trackerVersion == null) ? 0 : trackerVersion.hashCode());
      hashCode = prime * result + ((userId == null) ? 0 : userId.hashCode());
    }
    return hashCode;
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
      if (activityId == null) {
        if (other.activityId != null)
          return false;
      } else if (!activityId.equals(other.activityId))
        return false;
      if (startTime == null) {
        if (other.startTime != null)
          return false;
      } else if (!startTime.equals(other.startTime))
        return false;
      if (trackerVersion == null) {
        if (other.trackerVersion != null)
          return false;
      } else if (!trackerVersion.equals(other.trackerVersion))
        return false;
      if (userId == null) {
        if (other.userId != null)
          return false;
      } else if (!userId.equals(other.userId))
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
    s.append(userId);
    s.append('@');
    s.append(activityId);
    s.append('-');
    s.append(startTime);
    s.append('-');
    s.append(super.toString());
    return s.toString();
  }

}
