package org.exoplatform.smartactivitystream.admin.model;

/**
 * The Class CustomUserConnection (created in order to return correct json
 * representation of the user connection).
 */

public class CustomUserConnection {
  /** The user connection id. */
  private String id;

  /** The full name of the user connection. */
  private String fullName;

  /**
   * Instantiates a new CustomUserConnection.
   *
   * @param id the id
   * @param fullName the full name
   */
  public CustomUserConnection(String id, String fullName) {
    this.id = id;
    this.fullName = fullName;
  }

  /**
   * Gets the user connection id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the user connection id.
   *
   * @param id the user connection id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the full name of the user connection.
   *
   * @return the fullName
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * Sets the full name of the user connection.
   *
   * @param fullName the full name of the user connection to set
   */
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }
}
