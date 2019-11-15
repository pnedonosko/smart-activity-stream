package org.exoplatform.smartactivitystream.stats.domain;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

@Entity(name = "SmartActivityStatsAggregator")
@ExoEntity
public class ActivityStatsAggregatorEntity implements Externalizable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long                      id;

  @Transient
  private List<ActivityStatsEntity> activityStatsEntities;

  @Transient
  private Long                      maxTotalShown;

  public ActivityStatsAggregatorEntity(List<ActivityStatsEntity> activityStatsEntities, Long maxTotalShown) {
    this.activityStatsEntities = activityStatsEntities;
    this.maxTotalShown = maxTotalShown;
  }

  public List<ActivityStatsEntity> getActivityStatsEntities() {
    return activityStatsEntities;
  }

  public void setActivityStatsEntities(List<ActivityStatsEntity> activityStatsEntities) {
    this.activityStatsEntities = activityStatsEntities;
  }

  public Long getMaxTotalShown() {
    return maxTotalShown;
  }

  public void setMaxTotalShown(Long maxTotalShown) {
    this.maxTotalShown = maxTotalShown;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(getActivityStatsEntities());
    out.writeLong(getMaxTotalShown());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    setActivityStatsEntities((List<ActivityStatsEntity>) in.readObject());
    setMaxTotalShown(in.readLong());
  }
}
