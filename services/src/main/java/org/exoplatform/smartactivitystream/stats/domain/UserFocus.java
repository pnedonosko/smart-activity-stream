package org.exoplatform.smartactivitystream.stats.domain;

import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusEntity;

public class UserFocus extends ActivityFocusEntity {

  private String activityTitle;

  private long   activityCreated;

  private long   activityUpdated;

  public UserFocus(String activityTitle, long activityCreated, long activityUpdated) {
    this.activityTitle = activityTitle;
    this.activityCreated = activityCreated;
    this.activityUpdated = activityUpdated;

    super.activityId = "0";
    super.startTime = new Long(0);
    super.stopTime = new Long(0);
    super.totalShown = new Long(0);
    super.contentShown = new Long(0);
    super.convoShown = new Long(0);
    super.contentHits = new Long(0);
    super.convoHits = new Long(0);
    super.appHits = new Long(0);
    super.profileHits = new Long(0);
    super.linkHits = new Long(0);
    super.trackerVersion = "";
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
}
