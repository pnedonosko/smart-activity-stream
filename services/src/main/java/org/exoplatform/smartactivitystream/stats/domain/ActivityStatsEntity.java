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
@NamedNativeQueries({ @NamedNativeQuery(name = "SmartActivityStats.findActivityStats", query = "SELECT f.ACTIVITY_ID, "
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
    @NamedQuery(name = "SmartActivityStats.findActivityFocusChartData", query = "SELECT s.startTime, s.totalShown "
        + "FROM SmartActivityStats s WHERE s.activityId = :activityId ORDER BY s.startTime ASC"),
    @NamedQuery(name = "SmartActivityStats.findMaxTotalFocus", query = "SELECT MAX(s.totalShown) FROM SmartActivityStats s") })

public class ActivityStatsEntity extends BaseActivityFocusEntity {

  /** The Constant LOG. */
  protected static final Log    LOG         = ExoLogger.getLogger(ActivityStatsEntity.class);

  /** The date format for ActivityStats */
  protected static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

  protected static final Long   NULL_LONG   = new Long(-1);

  protected static final String NULL_STRING = new String("-1");

  /** The activity id. */
  @Id
  @Column(name = "ACTIVITY_ID", nullable = false)
  private String                activityId;

  /** The start time. */
  @Id
  @Column(name = "START_TIME", nullable = false)
  private Long                  startTime;

  private transient Locale      userLocale;

  @Transient
  private String                activityTitle;

  @Transient
  private String                activityCreated;

  private transient Long        activityCreatedMilliseconds;

  @Transient
  private String                activityUpdated;

  private transient Long        activityUpdatedMilliseconds;

  /*
   * The local start time of the user activity focus
   */
  @Transient
  private String                localStartTime;

  /*
   * The local stop time of the user activity focus
   */
  @Transient
  private String                localStopTime;

  @Transient
  private String                activityStreamPrettyId;

  @Transient
  private String[][]            focusChartData;

  /** The hash code. */
  private transient int         hashCode;

  public ActivityStatsEntity() {
  }

  private void setLocaleDateToData() {

    /*
     * Locale locale; try { locale = Util.getPortalRequestContext().getLocale(); }
     * catch (Exception e) {
     * LOG.warn("Cannot get locale from portal request context. {}", e); locale =
     * Locale.getDefault(); }
     */

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

    if (getFocusChartData() != null) {
      String[][] focuses = getFocusChartData();
      for (int i = 0; i < focuses.length; ++i) {
        focuses[i][0] = dateFormat.format(Long.parseLong(focuses[i][0]));
      }
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

  public void setUserLocale(Locale userLocale) {
    if (userLocale != null) {
      this.userLocale = userLocale;

      setLocaleDateToData();
    }
    setHashCode(0);
  }

  public String getActivityTitle() {
    return activityTitle;
  }

  public void setActivityTitle(String activityTitle) {
    this.activityTitle = activityTitle;
    setHashCode(0);
  }

  public String getActivityCreated() {
    return activityCreated;
  }

  public void setActivityCreated(String activityCreated) {
    this.activityCreated = activityCreated;
    setHashCode(0);
  }

  public String getActivityUpdated() {
    return activityUpdated;
  }

  public void setActivityUpdated(String activityUpdated) {
    this.activityUpdated = activityUpdated;
    setHashCode(0);
  }

  public Long getActivityCreatedMilliseconds() {
    return activityCreatedMilliseconds;
  }

  public void setActivityCreatedMilliseconds(Long activityCreatedMilliseconds) {
    if (activityCreatedMilliseconds != null) {
      this.activityCreatedMilliseconds = activityCreatedMilliseconds;
    } else {
      this.activityCreatedMilliseconds = NULL_LONG;
    }
    setHashCode(0);
  }

  public Long getActivityUpdatedMilliseconds() {
    return activityUpdatedMilliseconds;
  }

  public void setActivityUpdatedMilliseconds(Long activityUpdatedMilliseconds) {
    if (activityUpdatedMilliseconds != null) {
      this.activityUpdatedMilliseconds = activityUpdatedMilliseconds;
    } else {
      this.activityUpdatedMilliseconds = NULL_LONG;
    }
    setHashCode(0);
  }

  public String getLocalStartTime() {
    return localStartTime;
  }

  public void setLocalStartTime(String localStartTime) {
    this.localStartTime = localStartTime;
  }

  public String getLocalStopTime() {
    return localStopTime;
  }

  public void setLocalStopTime(String localStopTime) {
    this.localStopTime = localStopTime;
  }

  public String getActivityStreamPrettyId() {
    return activityStreamPrettyId;
  }

  public void setActivityStreamPrettyId(String activityStreamPrettyId) {
    this.activityStreamPrettyId = activityStreamPrettyId;
  }

  public String[][] getFocusChartData() {
    return focusChartData;
  }

  public void setFocusChartData(String[][] focusChartData) {
    this.focusChartData = focusChartData;
  }

  @Override
  public int getHashCode() {
    return hashCode;
  }

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
