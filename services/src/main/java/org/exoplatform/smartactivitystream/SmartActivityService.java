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

import java.util.Timer;
import java.util.TimerTask;

import javax.persistence.PersistenceException;

import org.picocontainer.Startable;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.stats.dao.ActivityFocusDAO;
import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusEntity;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: SmartActivityService.java 00000 Oct 2, 2019 pnedonosko $
 */
public class SmartActivityService implements Startable {

  /** The Constant LOG. */
  private static final Log                              LOG                  = ExoLogger.getLogger(SmartActivityService.class);

  /** The Constant TRACKER_CACHE_NAME. */
  public static final String                            TRACKER_CACHE_NAME   = "smartactivity.TrackerCache".intern();

  /** The Constant TRACKER_CACHE_PERIOD. */
  public static final int                               TRACKER_CACHE_PERIOD = 120000;

  /** Cache of tracking activities. */
  protected final ExoCache<String, ActivityFocusEntity> trackerCache;

  /** The focus storage. */
  protected final ActivityFocusDAO                      focusStorage;

  /** The focus saver. */
  protected final Timer                                 focusSaver           = new Timer();
  
  /** The enable trackers. */
  protected boolean enableTrackers = false;

  /**
   * Instantiates a new smart activity service.
   *
   * @param focusStorage the focus storage
   * @param cacheService the cache service
   * @param params the params
   */
  public SmartActivityService(ActivityFocusDAO focusStorage, CacheService cacheService, InitParams params) {
    this.focusStorage = focusStorage;
    this.trackerCache = cacheService.getCacheInstance(TRACKER_CACHE_NAME);
    
    // configuration
    PropertiesParam param = params.getPropertiesParam("smartactivity-configuration");
    if (param != null) {
      String enableTrackers = param.getProperties().get("enable-trackers");
      this.enableTrackers = enableTrackers != null ? Boolean.parseBoolean(enableTrackers.trim()) : true;
    }
  }

  /**
   * Submit activity focus.
   *
   * @param focus the focus
   * @throws SmartActivityException the smart activity exception
   */
  public void submitActivityFocus(ActivityFocusEntity focus) throws SmartActivityException {
    String fkey = focusKey(focus);
    ActivityFocusEntity tracked = trackerCache.get(fkey);
    if (tracked != null) {
      agregateFocus(tracked, focus);
      trackerCache.put(fkey, tracked); // this should sycn the cache in cluster
    } else {
      trackerCache.put(fkey, focus);
    }
  }
  
  /**
   * Checks if is trackers enabled.
   *
   * @return true, if is trackers enabled
   */
  public boolean isTrackersEnabled() {
    return this.enableTrackers;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
    final String containerName = ExoContainerContext.getCurrentContainer().getContext().getName();
    TimerTask saveTask = new TimerTask() {
      public void run() {
        saveReadyCacheInContainerContext(containerName, true);
      }
    };
    focusSaver.schedule(saveTask, TRACKER_CACHE_PERIOD * 2, TRACKER_CACHE_PERIOD);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    // We want to try save all cache on container stop
    saveReadyCacheInContainerContext(ExoContainerContext.getCurrentContainer().getContext().getName(), false);
  }

  /**
   * Focus key.
   *
   * @param focus the focus
   * @return the string
   */
  protected String focusKey(ActivityFocusEntity focus) {
    return new StringBuilder(focus.getUserId()).append(focus.getActivityId()).toString();
  }

  /**
   * Agregate focus.
   *
   * @param existing the existing
   * @param add the add
   */
  protected void agregateFocus(ActivityFocusEntity existing, ActivityFocusEntity add) {
    // Not null fields
    if (add.getStopTime() > existing.getStopTime()) {
      existing.setStopTime(add.getStopTime());
    }
    existing.setTotalShown(sum(existing.getTotalShown(), add.getTotalShown()));

    // Nullable fields
    existing.setContentShown(sum(existing.getContentShown(), add.getContentShown()));
    existing.setConvoShown(sum(existing.getConvoShown(), add.getConvoShown()));
    existing.setContentHits(sum(existing.getContentHits(), add.getContentHits()));
    existing.setConvoHits(sum(existing.getConvoHits(), add.getConvoHits()));
    existing.setAppHits(sum(existing.getAppHits(), add.getAppHits()));
    existing.setProfileHits(sum(existing.getProfileHits(), add.getProfileHits()));
    existing.setLinkHits(sum(existing.getLinkHits(), add.getLinkHits()));
  }

  /**
   * Sum.
   *
   * @param existing the existing
   * @param add the add
   * @return the long
   */
  protected Long sum(Long existing, Long add) {
    if (existing == null) {
      return add;
    }
    if (add == null) {
      return existing;
    }
    return existing + add;
  }

  /**
   * Save activity focus.
   *
   * @param focus the focus
   * @throws SmartActivityException the smart activity exception
   */
  protected void saveActivityFocus(ActivityFocusEntity focus) throws SmartActivityException {
    //
    try {
      ActivityFocusEntity tracked = focusStorage.find(focus.getId());
      if (tracked == null) {
        focusStorage.create(focus);
      } else {
        // We replace if of same version
        if (tracked.getTrackerVersion().equals(focus.getTrackerVersion())) {
          focusStorage.update(focus);
        } else {
          LOG.warn("Cannot update activity focus of different tracker versions: " + tracked.getTrackerVersion() + " vs "
              + focus.getTrackerVersion());
          throw new SmartActivityException("Cannot update activity focus of different tracker versions");
        }
      }
      // } catch (EntityExistsException e) {
      // LOG.error("Activity focus already tracked {}:{}", focus.getUserId(), focus.getActivityId(), e);
    } catch (PersistenceException e) {
      LOG.error("Failed to save activity focus {}:{}", focus.getUserId(), focus.getActivityId(), e);
      throw new SmartActivityException("Failed to save activity focus", e);
    }
  }

  /**
   * Save tracker cache.
   *
   * @param readyOnly the ready only
   * @throws Exception the exception
   */
  protected void saveTrackerCache(boolean readyOnly) throws Exception {
    trackerCache.select(new CachedObjectSelector<String, ActivityFocusEntity>() {

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean select(String key, ObjectCacheInfo<? extends ActivityFocusEntity> ocinfo) {
        if (readyOnly) {
          ActivityFocusEntity f = ocinfo.get();
          return f != null ? f.isReady() : false;
        }
        return true;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void onSelect(ExoCache<? extends String, ? extends ActivityFocusEntity> cache,
                           String key,
                           ObjectCacheInfo<? extends ActivityFocusEntity> ocinfo) throws Exception {
        ActivityFocusEntity f = ocinfo.get();
        if (f != null) {
          saveActivityFocus(f);
          cache.remove(key);
        }
      }
    });
  }

  /**
   * Save ready cache in container context.
   *
   * @param containerName the container name
   * @param readyOnly the ready only
   */
  protected void saveReadyCacheInContainerContext(String containerName, boolean readyOnly) {
    // Do the work under eXo container context (for proper work of eXo apps and JPA storage)
    ExoContainer exoContainer = ExoContainerContext.getContainerByName(containerName);
    if (exoContainer != null) {
      ExoContainer contextContainer = ExoContainerContext.getCurrentContainerIfPresent();
      try {
        // Container context
        ExoContainerContext.setCurrentContainer(exoContainer);
        RequestLifeCycle.begin(exoContainer);
        // do the work here
        saveTrackerCache(readyOnly);
      } catch (Exception e) {
        LOG.error("Error saving trackers", e);
      } finally {
        // Restore context
        RequestLifeCycle.end();
        ExoContainerContext.setCurrentContainer(contextContainer);
      }
    } else {
      LOG.warn("Container not found " + containerName + " for saving trackers");
    }
  }

}
