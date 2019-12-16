package org.exoplatform.smartactivitystream.stats.settings;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

public class GlobalSettings implements Serializable, Cloneable {

  private static final long serialVersionUID = 8987967110410722896L;

  // TODO this should be configurable via component plugin XML
  private String            accessPermission = "*:/platform/users"; // null "*:/platform/administrators"

  private boolean           enabled;

  /**
   * Instantiates a new GlobalSettings.
   * TODO we don't need this class
   */
  public GlobalSettings() {
  }

  /**
   * Instantiates a new GlobalSettings.
   *
   * @param accessPermission the access permission
   * @param enabled the enabled
   */
  public GlobalSettings(String accessPermission, boolean enabled) {
    this.accessPermission = accessPermission;
    this.enabled = enabled;
  }

  /**
   * Instantiates a new GlobalSettings (copy).
   *
   * @param globalSettings the access permission
   */
  public GlobalSettings(GlobalSettings globalSettings) {
    if (globalSettings != null) {
      try {
        BeanUtils.copyProperties(this, globalSettings);
      } catch (Exception e) {
        throw new IllegalStateException("Error while cloning attributes of global settings to current instance", e);
      }
    }
  }

  public GlobalSettings clone() { // NOSONAR
    try {
      return (GlobalSettings) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Error while cloning object: " + this, e);
    }
  }

  public String getAccessPermission() {
    return accessPermission;
  }

  public void setAccessPermission(String accessPermission) {
    this.accessPermission = accessPermission;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
