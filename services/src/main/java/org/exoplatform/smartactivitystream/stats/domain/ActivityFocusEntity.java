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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: ActivityFocus.java 00000 Oct 2, 2019 pnedonosko $
 */
@Entity(name = "SmartActivityFocus")
@ExoEntity
@Table(name = "ST_ACTIVITY_FOCUS")
@IdClass(FocusId.class)
@NamedQueries({
    @NamedQuery(name = "SmartActivityFocus.findTotalCount", query = "SELECT COUNT(f.activityId) FROM SmartActivityFocus f"),
    @NamedQuery(name = "SmartActivityFocus.findAllFocus", query = "SELECT f FROM SmartActivityFocus f "
        + "WHERE f.userId = :userId AND f.activityId = :activityId ORDER BY f.startTime ASC"),
    @NamedQuery(name = "SmartActivityFocus.findFocusAfter", query = "SELECT f FROM SmartActivityFocus f "
        + "WHERE f.userId = :userId AND f.activityId = :activityId AND f.startTime >= :startTimeAfter ORDER BY f.startTime DESC"),
    @NamedQuery(name = "SmartActivityFocus.findAllFocusOfUser", query = "SELECT f FROM SmartActivityFocus f "
        + "WHERE f.userId = :userId ORDER BY f.startTime") })

public class ActivityFocusEntity implements Externalizable {

  private static final Long  NULL_LONG       = new Long(-1);

  /** The Constant TRACKER_VERSION. */
  public static final String TRACKER_VERSION = "1.0";

  /** The user id. */
  @Id
  @Column(name = "USER_ID", nullable = false)
  protected String           userId;

  /** The activity id. */
  @Id
  @Column(name = "ACTIVITY_ID", nullable = false)
  protected String           activityId;

  /** The start time. */
  @Id
  @Column(name = "START_TIME", nullable = false)
  protected Long             startTime;

  /** The stop time. */
  @Column(name = "STOP_TIME", nullable = false)
  protected Long             stopTime;

  /** The total show time. */
  @Column(name = "TOTAL_SHOWN", nullable = false)
  protected Long             totalShown;

  /** The content show time. */
  @Column(name = "CONTENT_SHOWN")
  protected Long             contentShown;

  /** The conversation show time. */
  @Column(name = "CONVO_SHOWN")
  protected Long             convoShown;

  /** The content hits. */
  @Column(name = "CONTENT_HITS")
  protected Long             contentHits;

  /** The convo hits. */
  @Column(name = "CONVO_HITS")
  protected Long             convoHits;

  /** The app hits. */
  @Column(name = "APP_HITS")
  protected Long             appHits;

  /** The profile hits. */
  @Column(name = "PROFILE_HITS")
  protected Long             profileHits;

  /** The link hits. */
  @Column(name = "LINK_HITS")
  protected Long             linkHits;

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
   * Gets the stop time.
   *
   * @return the stopTime
   */
  public Long getStopTime() {
    return stopTime;
  }

  /**
   * Sets the stop time.
   *
   * @param stopTime the stopTime to set
   */
  public void setStopTime(Long stopTime) {
    this.stopTime = stopTime;
    this.hashCode = 0;
  }

  /**
   * Gets the total show.
   *
   * @return the totalShow
   */
  public Long getTotalShown() {
    return totalShown;
  }

  /**
   * Sets the total show.
   *
   * @param totalShow the totalShow to set
   */
  public void setTotalShown(Long totalShow) {
    this.totalShown = totalShow;
    this.hashCode = 0;
  }

  /**
   * Gets the content show.
   *
   * @return the contentShow
   */
  public Long getContentShown() {
    return contentShown;
  }

  /**
   * Sets the content show.
   *
   * @param contentShow the contentShow to set
   */
  public void setContentShown(Long contentShow) {
    this.contentShown = contentShow;
    this.hashCode = 0;
  }

  /**
   * Gets the convo show.
   *
   * @return the convoShow
   */
  public Long getConvoShown() {
    return convoShown;
  }

  /**
   * Sets the convo show.
   *
   * @param convoShow the convoShow to set
   */
  public void setConvoShown(Long convoShow) {
    this.convoShown = convoShow;
    this.hashCode = 0;
  }

  /**
   * Gets the content hits.
   *
   * @return the contentHits
   */
  public Long getContentHits() {
    return contentHits;
  }

  /**
   * Sets the content hits.
   *
   * @param contentHits the contentHits to set
   */
  public void setContentHits(Long contentHits) {
    this.contentHits = contentHits;
    this.hashCode = 0;
  }

  /**
   * Gets the convo hits.
   *
   * @return the convoHits
   */
  public Long getConvoHits() {
    return convoHits;
  }

  /**
   * Sets the convo hits.
   *
   * @param convoHits the convoHits to set
   */
  public void setConvoHits(Long convoHits) {
    this.convoHits = convoHits;
    this.hashCode = 0;
  }

  /**
   * Gets the app hits.
   *
   * @return the appHits
   */
  public Long getAppHits() {
    return appHits;
  }

  /**
   * Sets the app hits.
   *
   * @param appHits the appHits to set
   */
  public void setAppHits(Long appHits) {
    this.appHits = appHits;
    this.hashCode = 0;
  }

  /**
   * Gets the profile hits.
   *
   * @return the profileHits
   */
  public Long getProfileHits() {
    return profileHits;
  }

  /**
   * Sets the profile hits.
   *
   * @param profileHits the profileHits to set
   */
  public void setProfileHits(Long profileHits) {
    this.profileHits = profileHits;
    this.hashCode = 0;
  }

  /**
   * Gets the link hits.
   *
   * @return the linkHits
   */
  public Long getLinkHits() {
    return linkHits;
  }

  /**
   * Sets the link hits.
   *
   * @param linkHits the linkHits to set
   */
  public void setLinkHits(Long linkHits) {
    this.linkHits = linkHits;
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
   * Gets the id.
   *
   * @return the id
   */
  @Transient
  public FocusId getId() {
    return new FocusId(userId, activityId, startTime);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    // Always have value
    out.writeUTF(userId);
    out.writeUTF(activityId);
    out.writeUTF(trackerVersion);
    out.writeLong(startTime);
    out.writeLong(stopTime);
    out.writeLong(totalShown);
    // Nullable
    out.writeLong(contentShown != null ? contentShown : NULL_LONG);
    out.writeLong(convoShown != null ? convoShown : NULL_LONG);
    out.writeLong(contentHits != null ? contentHits : NULL_LONG);
    out.writeLong(convoHits != null ? convoHits : NULL_LONG);
    out.writeLong(appHits != null ? appHits : NULL_LONG);
    out.writeLong(profileHits != null ? profileHits : NULL_LONG);
    out.writeLong(linkHits != null ? linkHits : NULL_LONG);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    hashCode = 0;
    // Always have value
    userId = in.readUTF();
    activityId = in.readUTF();
    trackerVersion = in.readUTF();
    startTime = in.readLong();
    stopTime = in.readLong();
    totalShown = in.readLong();
    // Nullable
    Long l;
    contentShown = (l = in.readLong()) != NULL_LONG ? l : null;
    convoShown = (l = in.readLong()) != NULL_LONG ? l : null;
    contentHits = (l = in.readLong()) != NULL_LONG ? l : null;
    convoHits = (l = in.readLong()) != NULL_LONG ? l : null;
    appHits = (l = in.readLong()) != NULL_LONG ? l : null;
    profileHits = (l = in.readLong()) != NULL_LONG ? l : null;
    linkHits = (l = in.readLong()) != NULL_LONG ? l : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    if (hashCode == 0) {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((activityId == null) ? 0 : activityId.hashCode());
      result = prime * result + ((appHits == null) ? 0 : appHits.hashCode());
      result = prime * result + ((contentHits == null) ? 0 : contentHits.hashCode());
      result = prime * result + ((contentShown == null) ? 0 : contentShown.hashCode());
      result = prime * result + ((convoHits == null) ? 0 : convoHits.hashCode());
      result = prime * result + ((convoShown == null) ? 0 : convoShown.hashCode());
      result = prime * result + ((linkHits == null) ? 0 : linkHits.hashCode());
      result = prime * result + ((profileHits == null) ? 0 : profileHits.hashCode());
      result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
      result = prime * result + ((stopTime == null) ? 0 : stopTime.hashCode());
      result = prime * result + ((totalShown == null) ? 0 : totalShown.hashCode());
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
      if (activityId == null) {
        if (other.activityId != null)
          return false;
      } else if (!activityId.equals(other.activityId))
        return false;
      if (appHits == null) {
        if (other.appHits != null)
          return false;
      } else if (!appHits.equals(other.appHits))
        return false;
      if (contentHits == null) {
        if (other.contentHits != null)
          return false;
      } else if (!contentHits.equals(other.contentHits))
        return false;
      if (contentShown == null) {
        if (other.contentShown != null)
          return false;
      } else if (!contentShown.equals(other.contentShown))
        return false;
      if (convoHits == null) {
        if (other.convoHits != null)
          return false;
      } else if (!convoHits.equals(other.convoHits))
        return false;
      if (convoShown == null) {
        if (other.convoShown != null)
          return false;
      } else if (!convoShown.equals(other.convoShown))
        return false;
      if (linkHits == null) {
        if (other.linkHits != null)
          return false;
      } else if (!linkHits.equals(other.linkHits))
        return false;
      if (profileHits == null) {
        if (other.profileHits != null)
          return false;
      } else if (!profileHits.equals(other.profileHits))
        return false;
      if (startTime == null) {
        if (other.startTime != null)
          return false;
      } else if (!startTime.equals(other.startTime))
        return false;
      if (stopTime == null) {
        if (other.stopTime != null)
          return false;
      } else if (!stopTime.equals(other.stopTime))
        return false;
      if (totalShown == null) {
        if (other.totalShown != null)
          return false;
      } else if (!totalShown.equals(other.totalShown))
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
    s.append(stopTime);
    return s.toString();
  }

}
