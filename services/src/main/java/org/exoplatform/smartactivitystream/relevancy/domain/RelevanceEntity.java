/*
 * 
 */
package org.exoplatform.smartactivitystream.relevancy.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

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
 * The RelevanceEntity class that represents user attitude to the Activity. The
 * Activity can be marked as relevant or irrelevant
 */
@Entity(name = "SmartActivityRelevance")
@ExoEntity
@Table(name = "ST_ACTIVITY_RELEVANCY")
@IdClass(RelevanceId.class)
@NamedQueries({
  @NamedQuery(name = "SmartActivityRelevance.findCount", query = "SELECT COUNT(s.activityId) FROM SmartActivityRelevance s") })
public class RelevanceEntity {

  /** The date format. */
  @Transient
  protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  /** The user id. */
  @Id
  @Column(name = "USER_ID")
  protected String  userId;

  /** The activity id. */
  @Id
  @Column(name = "ACTIVITY_ID")
  protected String  activityId;

  /** Indicates relevance of the Activity. */
  @Column(name = "IS_RELEVANT")
  protected Boolean relevant;

  /** The weight. */
  @Column(name = "WEIGHT")
  protected Double  weight;

  /** The update date. */
  @Column(name = "UPDATE_DATE")
  protected Date    updateDate;

  /** The weight date. */
  @Column(name = "WEIGHT_DATE")
  protected Date    weightDate;

  /**
   * Gets the weight date.
   *
   * @return weight date
   */
  public Date getWeightDate() {
    return weightDate;
  }

  /**
   * Sets the weight date.
   *
   * @param weightDate the new weight date
   */
  public void setWeightDate(Date weightDate) {
    this.weightDate = weightDate;
  }

  /**
   * Gets the update date.
   *
   * @return updateDate
   */
  public Date getUpdateDate() {
    return updateDate;
  }

  /**
   * Sets the update date.
   *
   * @param updateDate the new update date
   */
  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  /**
   * Gets the user id.
   *
   * @return user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Gets the weight.
   *
   * @return weight
   */
  public Double getWeight() {
    return weight;
  }

  /**
   * Sets the weight.
   *
   * @param weight the new weight
   */
  public void setWeight(Double weight) {
    this.weight = weight;
  }

  /**
   * Sets the user id.
   *
   * @param userId the new user id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Gets the activity id.
   *
   * @return the activity id
   */
  public String getActivityId() {
    return activityId;
  }

  /**
   * Sets the activity id.
   *
   * @param activityId the new activity id
   */
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  /**
   * Gets the relevance.
   *
   * @return the relevant
   */
  public Boolean getRelevant() {
    return relevant;
  }

  /**
   * Sets the relevance.
   *
   * @param relevant the new relevant
   */
  public void setRelevant(Boolean relevant) {
    this.relevant = relevant;
  }

  /**
   * Converts the RelevanceEntity to the String.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "RelevanceEntity [userId=" + userId + ", activityId=" + activityId + ", relevant=" + relevant + ", weight=" + weight
        + ", updateDate=" + updateDate + "]";
  }

}
