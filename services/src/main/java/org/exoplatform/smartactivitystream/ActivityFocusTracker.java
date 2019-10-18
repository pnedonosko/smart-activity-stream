/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.smartactivitystream;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusEntity;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: ActivityFocusCollector.java 00000 Oct 18, 2019 pnedonosko $
 */
public class ActivityFocusTracker implements Externalizable {

  /** The Constant BATCH_LIFETIME. */
  public static final int     BATCH_LIFETIME = 600000;

  /** The entity. */
  private ActivityFocusEntity entity;

  /** The initialization time. */
  private long                initTime;

  /** The locked. */
  private boolean             locked         = false;

  /** The hash code. */
  private int                 hashCode;

  /**
   * Instantiates a new activity focus batch (used by cache deserialization).
   */
  public ActivityFocusTracker() {
  }

  /**
   * Instantiates a new activity focus batch with given focus entity.
   *
   * @param entity the entity
   */
  ActivityFocusTracker(ActivityFocusEntity entity) {
    this.entity = entity;
    this.initTime = System.currentTimeMillis();
  }

  /**
   * Gets the entity.
   *
   * @return the entity
   */
  ActivityFocusEntity getEntity() {
    return entity;
  }

  /**
   * Gets the inits the time.
   *
   * @return the initTime
   */
  Long getInitTime() {
    return initTime;
  }

  /**
   * Checks if is save time.
   *
   * @return true, if is save time
   */
  boolean isReady() {
    return initTime > 0 ? System.currentTimeMillis() - initTime > BATCH_LIFETIME : false;
  }

  /**
   * Checks if is locked.
   *
   * @return true, if is locked
   */
  boolean isLocked() {
    return locked;
  }

  /**
   * Lock the focus and return previous state.
   *
   * @return true, if was already locked
   */
  boolean lock() {
    boolean prevState = locked;
    locked = true;
    return prevState;
  }

  /**
   * Unlock the focus and return previous state.
   *
   * @return true, if was unlocked from locked state
   */
  boolean unlock() {
    boolean prevState = locked;
    locked = false;
    return prevState;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(initTime);
    out.writeBoolean(locked);
    out.writeObject(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    initTime = in.readLong();
    locked = in.readBoolean();
    entity = (ActivityFocusEntity) in.readObject();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    if (hashCode == 0) {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((entity == null) ? 0 : entity.hashCode());
      result = prime * result + (int) (initTime ^ (initTime >>> 32));
      hashCode = prime * result + (locked ? 1231 : 1237);
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
    if (obj != null && ActivityFocusTracker.class.isAssignableFrom(obj.getClass())) {
      ActivityFocusTracker other = ActivityFocusTracker.class.cast(obj);
      if (entity == null) {
        if (other.entity != null)
          return false;
      } else if (!entity.equals(other.entity))
        return false;
      if (initTime != other.initTime)
        return false;
      if (locked != other.locked)
        return false;
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append(this.getClass().getSimpleName());
    s.append('-');
    s.append(initTime);
    if (locked) {
      s.append("(locked)");
    }
    s.append('[');
    s.append(entity.toString());
    s.append(']');
    return s.toString();
  }

}
