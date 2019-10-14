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
    @NamedQuery(name = "SmartActivityFocus.findFocus", query = "SELECT f FROM SmartActivityFocus f "
        + "WHERE f.userId = :userId AND f.activityId = :activityId AND f.startTime = :startTime"),
    @NamedQuery(name = "SmartActivityFocus.findAllFocus", query = "SELECT f FROM SmartActivityFocus f "
        + "WHERE f.userId = :userId AND f.activityId = :activityId ORDER BY f.startTime") })
public class ActivityFocusEntity implements Externalizable {

  public static final int    CONTINOUS_SESSION_TIME = 20000;

  private static final Long  NULL_LONG              = new Long(-1);

  /** The Constant TRACKER_VERSION. */
  public static final String TRACKER_VERSION        = "1.0";

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

  /** The initialization time. */
  protected transient Long   initTime;

  /**
   * Instantiates a new activity focus.
   */
  public ActivityFocusEntity() {
    this.initTime = System.currentTimeMillis();
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
  }

  /**
   * Gets the inits the time.
   *
   * @return the initTime
   */
  @Transient
  public Long getInitTime() {
    return initTime;
  }

  /**
   * Checks if is save time.
   *
   * @return true, if is save time
   */
  @Transient
  public boolean isReady() {
    return System.currentTimeMillis() - initTime > CONTINOUS_SESSION_TIME;
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
    out.writeLong(initTime);
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
    // Always have value
    initTime = in.readLong();
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
  public String toString() {
    StringBuilder s = new StringBuilder();
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
