package org.exoplatform.smartactivitystream.stats.domain;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.persistence.*;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko & Nick
 *         Riabovol</a>
 * @version $Id: ActivityStatsEntity.java 00000 Nov 5, 2019 pnedonosko $
 */
@Entity(name = "SmartActivityStats")
@ExoEntity
@Table(name = "ST_ACTIVITY_FOCUS")
@IdClass(ActivityStatsId.class)
@NamedNativeQueries({
    @NamedNativeQuery(name = "SmartActivityStats.findActivityStats", query = "SELECT f.ACTIVITY_ID, "
        + "MIN(f.START_TIME) AS START_TIME, MAX(f.STOP_TIME) AS STOP_TIME, SUM(f.TOTAL_SHOWN) AS TOTAL_SHOWN, "
        + "SUM(f.CONTENT_SHOWN) AS CONTENT_SHOWN, SUM(f.CONVO_SHOWN) AS CONVO_SHOWN, SUM(f.CONTENT_HITS) AS CONTENT_HITS, "
        + "SUM(f.CONVO_HITS) AS CONVO_HITS, SUM(f.APP_HITS) AS APP_HITS, SUM(f.PROFILE_HITS) AS PROFILE_HITS, "
        + "SUM(f.LINK_HITS) AS LINK_HITS FROM ST_ACTIVITY_FOCUS f "
        + "WHERE f.ACTIVITY_ID = :activityId GROUP BY f.ACTIVITY_ID", resultClass = ActivityStatsEntity.class),
    @NamedNativeQuery(name = "SmartActivityStats.findActivityFocuses", query = "SELECT MAX(f.ACTIVITY_ID) AS ACTIVITY_ID, "
        + "MIN(f.START_TIME) AS START_TIME, MAX(f.STOP_TIME) AS STOP_TIME, SUM(f.TOTAL_SHOWN) AS TOTAL_SHOWN, "
        + "SUM(f.CONTENT_SHOWN) AS CONTENT_SHOWN, SUM(f.CONVO_SHOWN) AS CONVO_SHOWN, SUM(f.CONTENT_HITS) AS CONTENT_HITS, "
        + "SUM(f.CONVO_HITS) AS CONVO_HITS, SUM(f.APP_HITS) AS APP_HITS, SUM(f.PROFILE_HITS) AS PROFILE_HITS, "
        + "SUM(f.LINK_HITS) AS LINK_HITS FROM ST_ACTIVITY_FOCUS f WHERE f.ACTIVITY_ID = :activityId "
        + "GROUP BY (f.START_TIME - MOD(f.START_TIME,:scaleTime))/:scaleTime ORDER BY START_TIME DESC", resultClass = ActivityStatsEntity.class) })

@NamedQueries({
    @NamedQuery(name = "SmartActivityStats.findActivityFocusChartData", query = "SELECT NEW org.exoplatform.smartactivitystream.stats.domain.ChartPoint(s.startTime, s.totalShown) "
        + "FROM SmartActivityStats s WHERE s.activityId = :activityId ORDER BY s.startTime ASC"),
    @NamedQuery(name = "SmartActivityStats.findMaxTotalFocus", query = "SELECT MAX(s.totalShown) FROM SmartActivityStats s") })

public class ActivityStatsEntity extends BaseActivityFocusEntity {

  /** The Constant LOG. */
  protected static final Log    LOG         = ExoLogger.getLogger(ActivityStatsEntity.class);

  /** The date format for ActivityStats. */
  protected static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

  /** The null long value. */
  protected static final Long   NULL_LONG   = new Long(-1);

  /** The null string value. */
  protected static final String NULL_STRING = new String("-1");

  /** The activity id. */
  @Id
  @Column(name = "ACTIVITY_ID", nullable = false)
  private String                activityId;

  /** The start time. */
  @Id
  @Column(name = "START_TIME", nullable = false)
  private Long                  startTime;

  /** The current user locale. */
  private transient Locale      userLocale;

  /** The activity title. */
  @Transient
  private String                activityTitle;

  /**
   * The time of the current activity creation in the user locale representation.
   */
  @Transient
  private String                activityCreated;

  /** The time of the current activity creation in milliseconds. */
  private transient Long        activityCreatedMilliseconds;

  /**
   * The time of the current activity update in the user locale representation.
   */
  @Transient
  private String                activityUpdated;

  /** The time of the current activity update in milliseconds. */
  private transient Long        activityUpdatedMilliseconds;

  /**
   * The local start time of the user activity focus in the user locale
   * representation.
   */
  @Transient
  private String                localStartTime;

  /**
   * The local stop time of the user activity focus in the user locale
   * representation.
   */
  @Transient
  private String                localStopTime;

  /** The activity stream pretty id. */
  @Transient
  private String                activityStreamPrettyId;

  /** The activity URL (for the link of the title column in main table). */
  @Transient
  private String                activityUrl;

  /** The hash code. */
  private transient int         hashCode;

  /**
   * Instantiates a new ActivityStatsEntity.
   */
  public ActivityStatsEntity() {
  }

  /**
   * Sets the user locale date to user fields that have to be in the user locale.
   */
  private void setLocaleDateToData() {

    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, getUserLocale());

    if (getActivityCreatedMilliseconds() != null) {
      setActivityCreated(dateFormat.format(getActivityCreatedMilliseconds()));
    }

    if (getActivityUpdatedMilliseconds() != null) {
      setActivityUpdated(dateFormat.format(getActivityUpdatedMilliseconds()));
    }

    if (getStartTime() != null) {
      setLocalStartTime(dateFormat.format(getStartTime()));
    }

    if (getStopTime() != null) {
      setLocalStopTime(dateFormat.format(getStopTime()));
    }
  }

  @Override
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

  @Override
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

  @Transient
  public Locale getUserLocale() {
    return userLocale;
  }

  /**
   * Sets the user locale.
   *
   * @param userLocale the user locale to set
   */
  public void setUserLocale(Locale userLocale) {
    if (userLocale != null) {
      this.userLocale = userLocale;

      setLocaleDateToData();
    }
    setHashCode(0);
  }

  /**
   * Gets the activity title.
   *
   * @return the activityTitle
   */
  public String getActivityTitle() {
    return activityTitle;
  }

  /**
   * Sets the activity title.
   *
   * @param activityTitle the activity title to set
   */
  public void setActivityTitle(String activityTitle) {
    this.activityTitle = activityTitle;
    setHashCode(0);
  }

  /**
   * Gets the timestamp of the activity creation in the user locale
   * representation.
   *
   * @return the activityCreated
   */
  public String getActivityCreated() {
    return activityCreated;
  }

  /**
   * Sets the timestamp of the activity creation in the user locale
   * representation.
   *
   * @param activityCreated the timestamp of the activity creation to set
   */
  public void setActivityCreated(String activityCreated) {
    this.activityCreated = activityCreated;
    setHashCode(0);
  }

  /**
   * Gets the timestamp of the activity updated in the user locale representation.
   *
   * @return the activityUpdated
   */
  public String getActivityUpdated() {
    return activityUpdated;
  }

  /**
   * Sets the timestamp of the activity updated in the user locale representation.
   * 
   * @param activityUpdated the timestamp of the activity updated to set
   */
  public void setActivityUpdated(String activityUpdated) {
    this.activityUpdated = activityUpdated;
    setHashCode(0);
  }

  /**
   * Gets the timestamp of the activity creation in milliseconds.
   *
   * @return the activityCreatedMilliseconds
   */
  public Long getActivityCreatedMilliseconds() {
    return activityCreatedMilliseconds;
  }

  /**
   * Sets the timestamp of the activity creation in milliseconds.
   *
   * @param activityCreatedMilliseconds the timestamp of the activity creation in
   *          milliseconds to set
   */
  public void setActivityCreatedMilliseconds(Long activityCreatedMilliseconds) {
    if (activityCreatedMilliseconds != null) {
      this.activityCreatedMilliseconds = activityCreatedMilliseconds;
    } else {
      this.activityCreatedMilliseconds = NULL_LONG;
    }
    setHashCode(0);
  }

  /**
   * Gets the timestamp of the activity updated in milliseconds.
   *
   * @return the activityUpdatedMilliseconds
   */
  public Long getActivityUpdatedMilliseconds() {
    return activityUpdatedMilliseconds;
  }

  /**
   * Sets the timestamp of the activity updated in milliseconds.
   *
   * @param activityUpdatedMilliseconds the timestamp of the activity updated in
   *          milliseconds to set
   */
  public void setActivityUpdatedMilliseconds(Long activityUpdatedMilliseconds) {
    if (activityUpdatedMilliseconds != null) {
      this.activityUpdatedMilliseconds = activityUpdatedMilliseconds;
    } else {
      this.activityUpdatedMilliseconds = NULL_LONG;
    }
    setHashCode(0);
  }

  /**
   * Gets the start time in user locale.
   *
   * @return the localStartTime
   */
  public String getLocalStartTime() {
    return localStartTime;
  }

  /**
   * Sets the start time in user locale.
   *
   * @param localStartTime the start time in user locale to set
   */
  public void setLocalStartTime(String localStartTime) {
    this.localStartTime = localStartTime;
  }

  /**
   * Gets the stop time in user locale.
   *
   * @return the localStopTime
   */
  public String getLocalStopTime() {
    return localStopTime;
  }

  /**
   * Sets the stop time in user locale.
   *
   * @param localStopTime the stop time in user locale to set
   */
  public void setLocalStopTime(String localStopTime) {
    this.localStopTime = localStopTime;
  }

  /**
   * Gets the activity stream pretty id.
   *
   * @return the activityStreamPrettyId
   */
  public String getActivityStreamPrettyId() {
    return activityStreamPrettyId;
  }

  /**
   * Sets the activity stream pretty id.
   *
   * @param activityStreamPrettyId the activity stream pretty id to set
   */
  public void setActivityStreamPrettyId(String activityStreamPrettyId) {
    this.activityStreamPrettyId = activityStreamPrettyId;
  }

  /**
   * Gets the activity URL (uses in the main table activity title column, defines
   * the activity link ).
   *
   * @return the activityUrl
   */
  public String getActivityUrl() {
    return activityUrl;
  }

  /**
   * Sets the activity URL (uses in the main table activity title column, defines
   * the activity link ).
   *
   * @param activityUrl the activity URL to set
   */
  public void setActivityUrl(String activityUrl) {
    this.activityUrl = activityUrl;
  }

  /**
   * Gets the DATE_FORMAT of ActivityStatsEntity
   *
   * @return the DATE_FORMAT
   */
  public static String getDateFormat() {
    return DATE_FORMAT;
  }

  @Override
  public int getHashCode() {
    return hashCode;
  }

  /**
   * Sets the hash code.
   *
   * @param hashCode the hash code to set
   */
  public void setHashCode(int hashCode) {
    this.hashCode = hashCode;
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

    // Nullable
    out.writeObject(getUserLocale());
    out.writeUTF(getActivityTitle() != null ? getActivityTitle() : NULL_STRING);
    out.writeUTF(getActivityCreated() != null ? getActivityCreated() : NULL_STRING);
    out.writeLong(getActivityCreatedMilliseconds() != null ? getActivityCreatedMilliseconds() : NULL_LONG);
    out.writeUTF(getActivityUpdated() != null ? getActivityUpdated() : NULL_STRING);
    out.writeLong(getActivityUpdatedMilliseconds() != null ? getActivityUpdatedMilliseconds() : NULL_LONG);
    out.writeUTF(getLocalStartTime() != null ? getLocalStartTime() : NULL_STRING);
    out.writeUTF(getLocalStopTime() != null ? getLocalStopTime() : NULL_STRING);
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

    // Nullable
    Long l;
    String s;
    Locale locale;
    setUserLocale((locale = (Locale) in.readObject()) != null ? locale : null);
    setActivityTitle((s = in.readUTF()) != NULL_STRING ? s : null);
    setActivityCreated((s = in.readUTF()) != NULL_STRING ? s : null);
    setActivityCreatedMilliseconds((l = in.readLong()) != NULL_LONG ? l : null);
    setActivityUpdated((s = in.readUTF()) != NULL_STRING ? s : null);
    setActivityUpdatedMilliseconds((l = in.readLong()) != NULL_LONG ? l : null);
    setLocalStartTime((s = in.readUTF()) != NULL_STRING ? s : null);
    setLocalStopTime((s = in.readUTF()) != NULL_STRING ? s : null);
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
      result = prime * result + ((getUserLocale() == null) ? 0 : getUserLocale().hashCode());
      result = prime * result + ((getActivityTitle() == null) ? 0 : getActivityTitle().hashCode());
      result = prime * result + ((getActivityCreatedMilliseconds() == null) ? 0 : getActivityCreatedMilliseconds().hashCode());
      result = prime * result + ((getActivityUpdatedMilliseconds() == null) ? 0 : getActivityUpdatedMilliseconds().hashCode());
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
    if (obj != null && ActivityStatsEntity.class.isAssignableFrom(obj.getClass())) {
      ActivityStatsEntity other = ActivityStatsEntity.class.cast(obj);
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
      if (getUserLocale() == null) {
        if (other.getUserLocale() != null)
          return false;
      } else if (!getUserLocale().equals(other.getUserLocale()))
        return false;
      if (getActivityTitle() == null) {
        if (other.getActivityTitle() != null)
          return false;
      } else if (!getActivityTitle().equals(other.getActivityTitle()))
        return false;
      if (getActivityCreatedMilliseconds() == null) {
        if (other.getActivityCreatedMilliseconds() != null)
          return false;
      } else if (!getActivityCreatedMilliseconds().equals(other.getActivityCreatedMilliseconds()))
        return false;
      if (getActivityUpdatedMilliseconds() == null) {
        if (other.getActivityUpdatedMilliseconds() != null)
          return false;
      } else if (!getActivityUpdatedMilliseconds().equals(other.getActivityUpdatedMilliseconds()))
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
    s.append('@');
    s.append(getActivityId());
    s.append('-');
    s.append(getStartTime());
    s.append('-');
    s.append(getStopTime());
    return s.toString();
  }
}
