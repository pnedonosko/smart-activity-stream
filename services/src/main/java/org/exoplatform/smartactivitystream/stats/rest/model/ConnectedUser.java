package org.exoplatform.smartactivitystream.stats.rest.model;

public class ConnectedUser {

  private String userId;

  private String userFullName;

  public ConnectedUser(String userId, String userFullName) {
    this.userId = userId;
    this.userFullName = userFullName;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserFullName() {
    return userFullName;
  }

  public void setUserFullName(String userFullName) {
    this.userFullName = userFullName;
  }
}
