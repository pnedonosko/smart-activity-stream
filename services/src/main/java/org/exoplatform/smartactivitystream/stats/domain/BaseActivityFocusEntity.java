package org.exoplatform.smartactivitystream.stats.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.IdClass;
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

  /** The hash code. */
  private transient int      hashCode;

  /**
   * Instantiates a new activity focus.
   */
  public BaseActivityFocusEntity() {
  }

  public BaseActivityFocusEntity(Long stopTime,
                                 Long totalShown,
                                 Long contentShown,
                                 Long convoShown,
                                 Long contentHits,
                                 Long convoHits,
                                 Long appHits,
                                 Long profileHits,
                                 Long linkHits) {
    this.stopTime = stopTime;
    this.totalShown = totalShown;
    this.contentShown = contentShown;
    this.convoShown = convoShown;
    this.contentHits = contentHits;
    this.convoHits = convoHits;
    this.appHits = appHits;
    this.profileHits = profileHits;
    this.linkHits = linkHits;
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
   * {@inheritDoc}
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    // Always have value
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

      result = prime * result + ((appHits == null) ? 0 : appHits.hashCode());
      result = prime * result + ((contentHits == null) ? 0 : contentHits.hashCode());
      result = prime * result + ((contentShown == null) ? 0 : contentShown.hashCode());
      result = prime * result + ((convoHits == null) ? 0 : convoHits.hashCode());
      result = prime * result + ((convoShown == null) ? 0 : convoShown.hashCode());
      result = prime * result + ((linkHits == null) ? 0 : linkHits.hashCode());
      result = prime * result + ((profileHits == null) ? 0 : profileHits.hashCode());
      result = prime * result + ((stopTime == null) ? 0 : stopTime.hashCode());
      result = prime * result + ((totalShown == null) ? 0 : totalShown.hashCode());
      hashCode = result;
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
    s.append(" stopTime:");
    s.append(stopTime);
    s.append(";totalShown:");
    s.append(totalShown);
    s.append(";contentShown:");
    s.append(contentShown);
    s.append(";convoShown:");
    s.append(convoShown);
    s.append(";contentHits:");
    s.append(contentHits);
    s.append(";convoHits:");
    s.append(convoHits);
    s.append(";appHits:");
    s.append(appHits);
    s.append(";profileHits:");
    s.append(profileHits);
    s.append(";linkHits:");
    s.append(linkHits);
    return s.toString();
  }
}
