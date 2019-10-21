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

import java.security.PrivilegedAction;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.PersistenceException;

import org.picocontainer.Startable;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.BaseContainerLifecyclePlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
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
  private static final Log                               LOG                  = ExoLogger.getLogger(SmartActivityService.class);

  /** The Constant TRACKER_CACHE_NAME. */
  public static final String                             TRACKER_CACHE_NAME   = "smartactivity.TrackerCache".intern();

  /** The Constant TRACKER_CACHE_PERIOD. */
  public static final int                                TRACKER_CACHE_PERIOD = 120000;

  /** Cache of tracking activities. */
  private final ExoCache<String, ActivityFocusTracker> trackerCache;

  /** The focus storage. */
  private final ActivityFocusDAO                       focusStorage;

  /** The focus saver. */
  private final Timer                                    focusSaver           = new Timer();

  /** The focus saver started. */
  private final AtomicBoolean                            focusSaverStarted    = new AtomicBoolean(false);

  /** The enable trackers. */
  private final boolean                                enableTrackers;

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
    } else {
      this.enableTrackers = false;
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
    ActivityFocusTracker tracker = trackerCache.get(fkey);
    if (tracker != null) {
      agregateFocus(tracker, focus);
      trackerCache.put(fkey, tracker); // this should sycn the cache in cluster
    } else {
      trackerCache.put(fkey, new ActivityFocusTracker(focus));
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
    final ExoContainer container = ExoContainerContext.getCurrentContainer();
    TimerTask saveTask = new TimerTask() {
      public void run() {
        saveReadyCacheInContainerContext(container, true);
      }
    };
    focusSaver.schedule(saveTask, TRACKER_CACHE_PERIOD * 2, TRACKER_CACHE_PERIOD);
    focusSaverStarted.set(true);

    // We want to try save all trackers on machine stop, but before actual stop flow will start to get JPA layer not stopped
    // First we react on System.exit(), then on eXo container stop - a first will do the job
    SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>() {
      public Void run() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
          /**
           * {@inheritDoc}
           */
          @Override
          public void run() {
            SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>() {
              public Void run() {
                stopTracker(container);
                return null;
              }
            });
          }
        });
        return null;
      }
    });
    container.addContainerLifecylePlugin(new BaseContainerLifecyclePlugin() {
      /**
       * {@inheritDoc}
       */
      @Override
      public void stopContainer(ExoContainer container) {
        stopTracker(container);
      }
    });

    // When cache will evict a tracker, if not yet saved, we save it immediately
    trackerCache.addCacheListener(new CacheListener<String, ActivityFocusTracker>() {

      /**
       * {@inheritDoc}
       */
      @Override
      public void onExpire(CacheListenerContext context, String key, ActivityFocusTracker tracker) throws Exception {
        // We will save on cache expiration
        if (!tracker.isLocked()) {
          try {
            tracker.lock();
            saveActivityFocus(tracker.getEntity());
          } finally {
            tracker.unlock();
          }
        }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void onRemove(CacheListenerContext context, String key, ActivityFocusTracker obj) throws Exception {
        // Nothings
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void onPut(CacheListenerContext context, String key, ActivityFocusTracker obj) throws Exception {
        // Nothings
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void onGet(CacheListenerContext context, String key, ActivityFocusTracker obj) throws Exception {
        // Nothings
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void onClearCache(CacheListenerContext context) throws Exception {
        // Nothings
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    // TODO cleanup
    // focusSaver.cancel();
    // We want to try save all cache on container stop
    // saveReadyCacheInContainerContext(ExoContainerContext.getCurrentContainer().getContext().getName(), false);
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
   * Aggregate focus adding focus into the tracker.
   *
   * @param tracker the tracker
   * @param add the adding focus
   */
  protected void agregateFocus(ActivityFocusTracker tracker, ActivityFocusEntity add) {
    ActivityFocusEntity tracked = tracker.getEntity();
    // Not null fields
    if (add.getStopTime() > tracked.getStopTime()) {
      tracked.setStopTime(add.getStopTime());
    }
    tracked.setTotalShown(sum(tracked.getTotalShown(), add.getTotalShown()));

    // Nullable fields
    tracked.setContentShown(sum(tracked.getContentShown(), add.getContentShown()));
    tracked.setConvoShown(sum(tracked.getConvoShown(), add.getConvoShown()));
    tracked.setContentHits(sum(tracked.getContentHits(), add.getContentHits()));
    tracked.setConvoHits(sum(tracked.getConvoHits(), add.getConvoHits()));
    tracked.setAppHits(sum(tracked.getAppHits(), add.getAppHits()));
    tracked.setProfileHits(sum(tracked.getProfileHits(), add.getProfileHits()));
    tracked.setLinkHits(sum(tracked.getLinkHits(), add.getLinkHits()));
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
  @ExoTransactional
  protected void saveActivityFocus(ActivityFocusEntity focus) throws SmartActivityException {
    //
    if (LOG.isDebugEnabled()) {
      LOG.debug(">> saveActivityFocus: " + focus);
    }
    try {
      ActivityFocusEntity tracked = focusStorage.find(focus.getId());
      if (tracked == null) {
        focusStorage.create(focus);
        LOG.debug("<< saveActivityFocus => created: " + focus);
      } else {
        // We replace if of same version
        if (tracked.getTrackerVersion().equals(focus.getTrackerVersion())) {
          focusStorage.update(focus);
          LOG.debug("<< saveActivityFocus => updated: " + focus);
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
  @ExoTransactional
  protected void saveTrackerCache(boolean readyOnly) throws Exception {
    trackerCache.select(new CachedObjectSelector<String, ActivityFocusTracker>() {

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean select(String key, ObjectCacheInfo<? extends ActivityFocusTracker> ocinfo) {
        if (readyOnly) {
          ActivityFocusTracker ft = ocinfo.get();
          return ft != null ? ft.isReady() && !ft.isLocked() : false;
        }
        return true;
      }

      /**
       * {@inheritDoc}
       */
      @SuppressWarnings("unchecked")
      @Override
      public void onSelect(ExoCache<? extends String, ? extends ActivityFocusTracker> cache,
                           String key,
                           ObjectCacheInfo<? extends ActivityFocusTracker> ocinfo) throws Exception {
        ActivityFocusTracker ft = ocinfo.get();
        if (ft != null) {
          boolean cacheUnlocked = true;
          try {
            ft.lock();
            ((ExoCache<String, ActivityFocusTracker>) cache).put(key, ft); // let others know it's locked
            saveActivityFocus(ft.getEntity());
            cache.remove(key);
            cacheUnlocked = false;
          } finally {
            // If an error will happen during the save, we will keep the focus for a next attempt,
            // otherwise unlock has no actual sense.
            ft.unlock();
            if (cacheUnlocked) {
              ((ExoCache<String, ActivityFocusTracker>) cache).put(key, ft);
            }
          }
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
  protected void saveReadyCacheInContainerContext(ExoContainer exoContainer, boolean readyOnly) {
    // Do the work under eXo container context (for proper work of eXo apps and JPA storage)
    //ExoContainer exoContainer = ExoContainerContext.getContainerByName(containerName);
    if (exoContainer != null) {
      //ExoContainer contextContainer = ExoContainerContext.getCurrentContainerIfPresent();
      try {
        // Container context
        //ExoContainerContext.setCurrentContainer(exoContainer);
        RequestLifeCycle.begin(exoContainer);
        // do the work here
        saveTrackerCache(readyOnly);
      } catch (Exception e) {
        LOG.error("Error saving trackers", e);
      } finally {
        // Restore context
        RequestLifeCycle.end();
        //ExoContainerContext.setCurrentContainer(contextContainer);
      }
    } else {
      LOG.warn("Container not found " + exoContainer + " for saving trackers");
    }
  }

  private void stopTracker(ExoContainer container) {
    if (focusSaverStarted.getAndSet(false)) {
      focusSaver.cancel();
      saveReadyCacheInContainerContext(container, false);
    }
  }

}
