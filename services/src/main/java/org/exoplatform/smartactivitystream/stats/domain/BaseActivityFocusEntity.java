package org.exoplatform.smartactivitystream.stats.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@MappedSuperclass
public abstract class BaseActivityFocusEntity implements Externalizable {

  private static final Long  NULL_LONG       = new Long(-1);

  /** The Constant TRACKER_VERSION. */
  public static final String TRACKER_VERSION = "1.0";

  /** The stop time in milliseconds. */
  @Column(name = "STOP_TIME", nullable = false)
  private Long               stopTime;

  /** The total show time. */
  @Column(name = "TOTAL_SHOWN", nullable = false)
  private Long               totalShown;

  /** The content show time. */
  @Column(name = "CONTENT_SHOWN")
  private Long               contentShown;

  /** The conversation show time. */
  @Column(name = "CONVO_SHOWN")
  private Long               convoShown;

  /** The content hits. */
  @Column(name = "CONTENT_HITS")
  private Long               contentHits;

  /** The convo hits. */
  @Column(name = "CONVO_HITS")
  private Long               convoHits;

  /** The app hits. */
  @Column(name = "APP_HITS")
  private Long               appHits;

  /** The profile hits. */
  @Column(name = "PROFILE_HITS")
  private Long               profileHits;

  /** The link hits. */
  @Column(name = "LINK_HITS")
  private Long               linkHits;

  /** The hash code. */
  private transient int      hashCode;

  /**
   * Instantiates a new activity focus.
   */
  public BaseActivityFocusEntity() {
  }

  /**
   * Gets the activity id.
   *
   * @return the activityId
   */
  public abstract String getActivityId();

  /**
   * Gets the start time.
   *
   * @return the startTime
   */
  public abstract Long getStartTime();

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
    setHashCode(0);
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
    setHashCode(0);
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
    setHashCode(0);
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
    setHashCode(0);
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
    setHashCode(0);
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
    setHashCode(0);
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
    setHashCode(0);
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
    setHashCode(0);
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
    setHashCode(0);
  }

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
  public void writeExternal(ObjectOutput out) throws IOException {
    // Always have value
    out.writeLong(getStopTime());
    out.writeLong(getTotalShown());
    // Nullable
    out.writeLong(getContentShown() != null ? getContentShown() : NULL_LONG);
    out.writeLong(getConvoShown() != null ? getConvoShown() : NULL_LONG);
    out.writeLong(getContentHits() != null ? getContentHits() : NULL_LONG);
    out.writeLong(getConvoHits() != null ? getConvoHits() : NULL_LONG);
    out.writeLong(getAppHits() != null ? getAppHits() : NULL_LONG);
    out.writeLong(getProfileHits() != null ? getProfileHits() : NULL_LONG);
    out.writeLong(getLinkHits() != null ? getLinkHits() : NULL_LONG);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    setHashCode(0);
    // Always have value
    setStopTime(in.readLong());
    setTotalShown(in.readLong());
    // Nullable
    Long l;
    setContentShown((l = in.readLong()) != NULL_LONG ? l : null);
    setConvoShown((l = in.readLong()) != NULL_LONG ? l : null);
    setContentHits((l = in.readLong()) != NULL_LONG ? l : null);
    setConvoHits((l = in.readLong()) != NULL_LONG ? l : null);
    setAppHits((l = in.readLong()) != NULL_LONG ? l : null);
    setProfileHits((l = in.readLong()) != NULL_LONG ? l : null);
    setLinkHits((l = in.readLong()) != NULL_LONG ? l : null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    if (getHashCode() == 0) {
      final int prime = 31;
      int result = 1;

      result = prime * result + ((getAppHits() == null) ? 0 : getAppHits().hashCode());
      result = prime * result + ((getContentHits() == null) ? 0 : getContentHits().hashCode());
      result = prime * result + ((getContentShown() == null) ? 0 : getContentShown().hashCode());
      result = prime * result + ((getConvoHits() == null) ? 0 : getConvoHits().hashCode());
      result = prime * result + ((getConvoShown() == null) ? 0 : getConvoShown().hashCode());
      result = prime * result + ((getLinkHits() == null) ? 0 : getLinkHits().hashCode());
      result = prime * result + ((getProfileHits() == null) ? 0 : getProfileHits().hashCode());
      result = prime * result + ((getStopTime() == null) ? 0 : getStopTime().hashCode());
      result = prime * result + ((getTotalShown() == null) ? 0 : getTotalShown().hashCode());
      setHashCode(result);
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
      if (getAppHits() == null) {
        if (other.getAppHits() != null)
          return false;
      } else if (!getAppHits().equals(other.getAppHits()))
        return false;
      if (getContentHits() == null) {
        if (other.getContentHits() != null)
          return false;
      } else if (!getContentHits().equals(other.getContentHits()))
        return false;
      if (getContentShown() == null) {
        if (other.getContentShown() != null)
          return false;
      } else if (!getContentShown().equals(other.getContentShown()))
        return false;
      if (getConvoHits() == null) {
        if (other.getConvoHits() != null)
          return false;
      } else if (!getConvoHits().equals(other.getConvoHits()))
        return false;
      if (getConvoShown() == null) {
        if (other.getConvoShown() != null)
          return false;
      } else if (!getConvoShown().equals(other.getConvoShown()))
        return false;
      if (getLinkHits() == null) {
        if (other.getLinkHits() != null)
          return false;
      } else if (!getLinkHits().equals(other.getLinkHits()))
        return false;
      if (getProfileHits() == null) {
        if (other.getProfileHits() != null)
          return false;
      } else if (!getProfileHits().equals(other.getProfileHits()))
        return false;
      if (getStopTime() == null) {
        if (other.getStopTime() != null)
          return false;
      } else if (!getStopTime().equals(other.getStopTime()))
        return false;
      if (getTotalShown() == null) {
        if (other.getTotalShown() != null)
          return false;
      } else if (!getTotalShown().equals(other.getTotalShown()))
        return false;
      return true;
    }
    return false;
  }
}
