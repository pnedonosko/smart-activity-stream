package org.exoplatform.smartactivitystream.stats.domain;

/**
 * The Class ChartPoint (for getting chart points array).
 */

public class ChartPoint {

  /** The point in time. */
  private String time;

  /** The value. */
  private String value;

  public ChartPoint(long time, long value) {
    this.time = Long.toString(time);
    this.value = Long.toString(value);
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
