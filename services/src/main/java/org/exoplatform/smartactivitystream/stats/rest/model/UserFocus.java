package org.exoplatform.smartactivitystream.stats.rest.model;

public class UserFocus {

  private String activityTitle;

  private long   activityCreated;

  private long   activityUpdated;

  private long   startTimeStatistics;

  private long   stopTimeStatistics;

  private long   totalFocusStatistics;

  private long   contentFocusStatistics;

  private long   convoFocusStatistics;

  private long   contentHitsStatistics;

  private long   convoHitsStatistics;

  private long   appHitsStatistics;

  private long   profileHitsStatistics;

  private long   linkHitsStatistics;

  public UserFocus(String activityTitle, long activityCreated, long activityUpdated) {
    this.activityTitle = activityTitle;
    this.activityCreated = activityCreated;
    this.activityUpdated = activityUpdated;
  }

  public String getActivityTitle() {
    return activityTitle;
  }

  public void setActivityTitle(String activityTitle) {
    this.activityTitle = activityTitle;
  }

  public long getActivityCreated() {
    return activityCreated;
  }

  public void setActivityCreated(long activityCreated) {
    this.activityCreated = activityCreated;
  }

  public long getActivityUpdated() {
    return activityUpdated;
  }

  public void setActivityUpdated(long activityUpdated) {
    this.activityUpdated = activityUpdated;
  }

  public long getStartTimeStatistics() {
    return startTimeStatistics;
  }

  public void setStartTimeStatistics(long startTimeStatistics) {
    this.startTimeStatistics = startTimeStatistics;
  }

  public long getStopTimeStatistics() {
    return stopTimeStatistics;
  }

  public void setStopTimeStatistics(long stopTimeStatistics) {
    this.stopTimeStatistics = stopTimeStatistics;
  }

  public long getTotalFocusStatistics() {
    return totalFocusStatistics;
  }

  public void setTotalFocusStatistics(long totalFocusStatistics) {
    this.totalFocusStatistics = totalFocusStatistics;
  }

  public long getContentFocusStatistics() {
    return contentFocusStatistics;
  }

  public void setContentFocusStatistics(long contentFocusStatistics) {
    this.contentFocusStatistics = contentFocusStatistics;
  }

  public long getConvoFocusStatistics() {
    return convoFocusStatistics;
  }

  public void setConvoFocusStatistics(long convoFocusStatistics) {
    this.convoFocusStatistics = convoFocusStatistics;
  }

  public long getContentHitsStatistics() {
    return contentHitsStatistics;
  }

  public void setContentHitsStatistics(long contentHitsStatistics) {
    this.contentHitsStatistics = contentHitsStatistics;
  }

  public long getConvoHitsStatistics() {
    return convoHitsStatistics;
  }

  public void setConvoHitsStatistics(long convoHitsStatistics) {
    this.convoHitsStatistics = convoHitsStatistics;
  }

  public long getAppHitsStatistics() {
    return appHitsStatistics;
  }

  public void setAppHitsStatistics(long appHitsStatistics) {
    this.appHitsStatistics = appHitsStatistics;
  }

  public long getProfileHitsStatistics() {
    return profileHitsStatistics;
  }

  public void setProfileHitsStatistics(long profileHitsStatistics) {
    this.profileHitsStatistics = profileHitsStatistics;
  }

  public long getLinkHitsStatistics() {
    return linkHitsStatistics;
  }

  public void setLinkHitsStatistics(long linkHitsStatistics) {
    this.linkHitsStatistics = linkHitsStatistics;
  }
}
