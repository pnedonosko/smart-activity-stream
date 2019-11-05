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
@NamedNativeQueries({ @NamedNativeQuery(name = "SmartActivityStats.findActivityStats", query = "SELECT f.ACTIVITY_ID, "
    + "MIN(f.START_TIME) AS START_TIME, MAX(f.STOP_TIME) AS STOP_TIME, SUM(f.TOTAL_SHOWN) AS TOTAL_SHOWN, "
    + "SUM(f.CONTENT_SHOWN) AS CONTENT_SHOWN, SUM(f.CONVO_SHOWN) AS CONVO_SHOWN, SUM(f.CONTENT_HITS) AS CONTENT_HITS, "
    + "SUM(f.CONVO_HITS) AS CONVO_HITS, SUM(f.APP_HITS) AS APP_HITS, SUM(f.PROFILE_HITS) AS PROFILE_HITS, "
    + "SUM(f.LINK_HITS) AS LINK_HITS FROM ST_ACTIVITY_FOCUS f WHERE f.ACTIVITY_ID = :activityId "
    + "GROUP BY f.ACTIVITY_ID", resultClass = ActivityStatsEntity.class) })

public class ActivityStatsEntity extends BaseActivityFocusEntity {

  /** The Constant LOG. */
  protected static final Log    LOG         = ExoLogger.getLogger(ActivityStatsEntity.class);

  /** The date format for ActivityStats */
  protected static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

  protected static final Long   NULL_LONG   = new Long(-1);

  protected static final String NULL_STRING = new String("-1");

  private transient Locale      userLocale;

  @Transient
  private String                activityTitle;

  @Transient
  private String                activityCreated;

  private transient Long        activityCreatedMilliseconds;

  @Transient
  private String                activityUpdated;

  private transient Long        activityUpdatedMilliseconds;

  /** The hash code. */
  private transient int         hashCode;

  public ActivityStatsEntity() {
  }

  public ActivityStatsEntity(String activityTitle,
                             Long activityCreated,
                             Long activityUpdated,
                             String activityId,
                             Locale userLocale) {

    setActivityTitle(activityTitle);

    setActivityCreatedMilliseconds(activityCreated);

    setActivityUpdatedMilliseconds(activityUpdated);

    setActivityId(activityId);

    setUserLocale(userLocale);
    this.hashCode = 0;
  }

  private void setLocaleDateToActivityData() {

    /*
     * Locale locale; try { locale = Util.getPortalRequestContext().getLocale(); }
     * catch (Exception e) {
     * LOG.warn("Cannot get locale from portal request context. {}", e); locale =
     * Locale.getDefault(); }
     */

    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, getUserLocale());

    if (activityCreatedMilliseconds != null) {
      setActivityCreated(dateFormat.format(activityCreatedMilliseconds));
    }

    if (activityUpdatedMilliseconds != null) {
      setActivityUpdated(dateFormat.format(activityUpdatedMilliseconds));
    }
  }

  @Transient
  public Locale getUserLocale() {
    return userLocale;
  }

  public void setUserLocale(Locale userLocale) {
    if (userLocale != null) {
      this.userLocale = userLocale;

      setLocaleDateToActivityData();
    }
    this.hashCode = 0;
  }

  public String getActivityTitle() {
    return activityTitle;
  }

  public void setActivityTitle(String activityTitle) {
    this.activityTitle = activityTitle;
    this.hashCode = 0;
  }

  public String getActivityCreated() {
    return activityCreated;
  }

  public void setActivityCreated(String activityCreated) {
    this.activityCreated = activityCreated;
    this.hashCode = 0;
  }

  public String getActivityUpdated() {
    return activityUpdated;
  }

  public void setActivityUpdated(String activityUpdated) {
    this.activityUpdated = activityUpdated;
    this.hashCode = 0;
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
    this.hashCode = 0;
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
    this.hashCode = 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);

    // Nullable
    out.writeObject(userLocale);
    out.writeUTF(activityTitle != null ? activityTitle : NULL_STRING);
    out.writeUTF(activityCreated != null ? activityCreated : NULL_STRING);
    out.writeLong(activityCreatedMilliseconds != null ? activityCreatedMilliseconds : NULL_LONG);
    out.writeUTF(activityUpdated != null ? activityUpdated : NULL_STRING);
    out.writeLong(activityUpdatedMilliseconds != null ? activityUpdatedMilliseconds : NULL_LONG);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.hashCode = 0;

    super.readExternal(in);

    // Nullable
    Long l;
    String s;
    Locale locale;
    userLocale = (locale = (Locale) in.readObject()) != null ? locale : null;
    activityTitle = (s = in.readUTF()) != NULL_STRING ? s : null;
    activityCreated = (s = in.readUTF()) != NULL_STRING ? s : null;
    activityCreatedMilliseconds = (l = in.readLong()) != NULL_LONG ? l : null;
    activityUpdated = (s = in.readUTF()) != NULL_STRING ? s : null;
    activityUpdatedMilliseconds = (l = in.readLong()) != NULL_LONG ? l : null;
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
      result = prime * result + ((userLocale == null) ? 0 : userLocale.hashCode());
      result = prime * result + ((activityTitle == null) ? 0 : activityTitle.hashCode());
      result = prime * result + ((activityCreatedMilliseconds == null) ? 0 : activityCreatedMilliseconds.hashCode());
      result = prime * result + ((activityUpdatedMilliseconds == null) ? 0 : activityUpdatedMilliseconds.hashCode());
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
    if (obj != null && ActivityStatsEntity.class.isAssignableFrom(obj.getClass())) {
      ActivityStatsEntity other = ActivityStatsEntity.class.cast(obj);
      if (!super.equals(obj)) {
        return false;
      }
      if (userLocale == null) {
        if (other.userLocale != null)
          return false;
      } else if (!userLocale.equals(other.userLocale))
        return false;
      if (activityTitle == null) {
        if (other.activityTitle != null)
          return false;
      } else if (!activityTitle.equals(other.activityTitle))
        return false;
      if (activityCreatedMilliseconds == null) {
        if (other.activityCreatedMilliseconds != null)
          return false;
      } else if (!activityCreatedMilliseconds.equals(other.activityCreatedMilliseconds))
        return false;
      if (activityUpdatedMilliseconds == null) {
        if (other.activityUpdatedMilliseconds != null)
          return false;
      } else if (!activityUpdatedMilliseconds.equals(other.activityUpdatedMilliseconds))
        return false;

      return true;
    }
    return false;
  }
}
