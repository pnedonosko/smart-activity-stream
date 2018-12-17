/*
 * 
 */
package org.exoplatform.smartactivitystream.relevancy.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Transient;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * The RelevanceStatsEntity class that represents statistics for relevance
 * records.
 */
@Entity(name = "SmartActivityRelevancyStats")
@ExoEntity
@NamedNativeQueries({ @NamedNativeQuery(name = "SmartActivityRelevancyStats.findStats", query = "SELECT user_id," //
    + " (SELECT COUNT(*) FROM ST_ACTIVITY_RELEVANCY WHERE user_id = r.user_id AND IS_RELEVANT = 1 AND update_date > :afterDate) AS relevant_count,"
    + " (SELECT COUNT(*) FROM ST_ACTIVITY_RELEVANCY WHERE user_id = r.user_id AND IS_RELEVANT = 0 AND update_date > :afterDate) AS irrelevant_count,"
    + " (SELECT COUNT(*) FROM ST_ACTIVITY_RELEVANCY WHERE user_id = r.user_id AND IS_RELEVANT IS NULL AND update_date > :afterDate) AS neutral_count,"
    + " MAX(r.update_date) AS last_date" //
    + " FROM ST_ACTIVITY_RELEVANCY r" //
    + " WHERE r.update_date > :afterDate" //
    + " GROUP BY r.user_id", resultClass = RelevanceStatsEntity.class) })
public class RelevanceStatsEntity {

  /** The date format. */
  @Transient
  protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  /** The user id. */
  @Id
  @Column(name = "user_id")
  protected String                 userId;

  /** The relevant count. */
  @Column(name = "relevant_count")
  protected Long                   relevantCount;

  /** The irrelevant count. */
  @Column(name = "irrelevant_count")
  protected Long                   irrelevantCount;

  /** The neutral count. */
  @Column(name = "neutral_count")
  protected Long                   neutralCount;

  /** The update date. */
  @Column(name = "last_date")
  protected Date                   lastDate;

  /**
   * Gets the user id.
   *
   * @return the user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Gets the total count.
   *
   * @return the total count
   */
  public Long getTotalCount() {
    return getRelevantCount() + getIrrelevantCount() + getNeutralCount();
  }

  /**
   * Gets the relevant count.
   *
   * @return the relevant count
   */
  public Long getRelevantCount() {
    return relevantCount;
  }

  /**
   * Gets the irrelevant count.
   *
   * @return the irrelevant count
   */
  public Long getIrrelevantCount() {
    return irrelevantCount;
  }

  /**
   * Gets the neutral count.
   *
   * @return the neutral count
   */
  public Long getNeutralCount() {
    return neutralCount;
  }

  /**
   * Gets the last update date.
   *
   * @return the last date
   */
  public String getUpdateDate() {
    return dateFormat.format(lastDate);
  }

  /**
   * Converts the RelevanceEntity to the String.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "RelevanceStatsEntity [userId=" + userId + ", lastDate=" + lastDate + "]";
  }

}
