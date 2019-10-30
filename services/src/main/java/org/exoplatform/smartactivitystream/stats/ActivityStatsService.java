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
package org.exoplatform.smartactivitystream.stats;

import java.security.PrivilegedAction;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jcr.*;
import javax.persistence.PersistenceException;

import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.exoplatform.social.core.profile.ProfileLoader;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
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
import org.exoplatform.smartactivitystream.stats.domain.FocusId;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: SmartActivityService.java 00000 Oct 2, 2019 pnedonosko $
 */
public class ActivityStatsService implements Startable {

  /** The Constant LOG. */
  private static final Log                             LOG                  = ExoLogger.getLogger(ActivityStatsService.class);

  /** The Constant TRACKER_CACHE_NAME. */
  public static final String                           TRACKER_CACHE_NAME   = "smartactivity.TrackerCache".intern();

  /** The Constant TRACKER_CACHE_PERIOD. */
  public static final int                              TRACKER_CACHE_PERIOD = 120000;

  /** Cache of tracking activities. */
  private final ExoCache<String, ActivityFocusTracker> trackerCache;

  /** The focus storage. */
  private final ActivityFocusDAO                       focusStorage;

  /** The focus saver. */
  private final Timer                                  focusSaver           = new Timer();

  /** The focus saver started. */
  private final AtomicBoolean                          focusSaverStarted    = new AtomicBoolean(false);

  /** The enable trackers. */
  private final boolean                                enableTrackers;

  /** The activity manager. */
  protected final ActivityManager                      activityManager;

  /** The identity manager. */
  protected final IdentityManager                      identityManager;

  /** The identity provider. */
  protected final IdentityProvider                     identityProvider;

  /** The space storage. */
  protected final SpaceStorage                         spaceStorage;

  /** The space storage. */
  protected final RelationshipStorage                  relationshipStorage;

  /**
   * Instantiates a new smart activity service.
   *
   * @param focusStorage the focus storage
   * @param cacheService the cache service
   * @param params the params
   */
  public ActivityStatsService(ActivityFocusDAO focusStorage,
                              CacheService cacheService,
                              InitParams params,
                              ActivityManager activityManager,
                              IdentityManagerImpl identityManager,
                              IdentityProvider identityProvider,
                              SpaceStorage spaceStorage,
                              RelationshipStorage relationshipStorage) {
    this.focusStorage = focusStorage;
    this.trackerCache = cacheService.getCacheInstance(TRACKER_CACHE_NAME);
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.identityProvider = identityProvider;
    this.spaceStorage = spaceStorage;
    this.relationshipStorage = relationshipStorage;

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
   * @throws ActivityStatsException the smart activity exception
   */
  public void submitActivityFocus(ActivityFocusEntity focus) throws ActivityStatsException {
    String fkey = focusKey(focus);
    ActivityFocusTracker tracker = trackerCache.get(fkey);
    if (tracker != null) {
      agregateFocus(tracker.getEntity(), focus);
      trackerCache.put(fkey, tracker); // this should sycn the cache in cluster
    } else {
      trackerCache.put(fkey, new ActivityFocusTracker(focus));
    }
  }

  /**
   * Save all cached trackers to database. This will save even not yet ready
   * batches (shorten than {@link ActivityFocusTracker#BATCH_LIFETIME}).
   */
  public void saveTrackers() {
    saveCacheInContainerContext(ExoContainerContext.getCurrentContainer(), false);
  }

  /**
   * Checks if is trackers enabled.
   *
   * @return true, if is trackers enabled
   */
  public boolean isTrackersEnabled() {
    return this.enableTrackers;
  }

  public ActivityManager getActivityManager() {

    return this.activityManager;
  }

  public List<ActivityFocusEntity> findAllFocusOfUser(String currentUserId) {
    List<ActivityFocusEntity> activityFocusRecords = null;

    activityFocusRecords = focusStorage.findAllFocusOfUser(currentUserId, 0, "");

    return activityFocusRecords;
  }

  protected IdentityManager socialIdentityManager() {
    return (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
  }

  public Identity getUserIdentity(String userId) {
    // identityProvider.getIdentityByRemoteId(userId)
    // profileLoader.load().getIdentity();
    // Profile userProfile = null;

    /*
     * SessionProvider sessionProvider =
     * sessionProviderService.getSessionProvider(null); Session session =
     * sessionProvider.getSession(repositoryService.getCurrentRepository().
     * getConfiguration().getDefaultWorkspaceName(),
     * repositoryService.getCurrentRepository());
     */

    // session.getU

    // IdentityManager identityManagerI = socialIdentityManager();

    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);

    /*
     * try { userProfile = userProfileHandler.findUserProfileByName(userId); } catch
     * (Exception e) { e.printStackTrace(); }
     */

    return userIdentity;
  }

  public List<Space> getUserSpaces(String userId) {
    return spaceStorage.getMemberSpaces(userId);
  }

  public List<Identity> getUserConnections(String userId) {
    return relationshipStorage.getConnections(getUserIdentity(userId));
  }

  public List<Identity> getLastIdentities(int n) {
    return identityManager.getLastIdentities(n);
  }

  /*
   * public ListAccess<Identity> getUserConnectionsFromManager(String userId) {
   * return relationshipManager.getConnections(getUserIdentity(userId)); }
   */

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
    final ExoContainer container = ExoContainerContext.getCurrentContainer();
    TimerTask saveTask = new TimerTask() {
      public void run() {
        saveCacheInContainerContext(container, true);
      }
    };
    focusSaver.schedule(saveTask, TRACKER_CACHE_PERIOD * 2, TRACKER_CACHE_PERIOD);
    focusSaverStarted.set(true);

    // We want to try save all trackers on machine stop, but before actual stop flow
    // will start to get JPA layer not stopped
    // First we react on System.exit(), then on eXo container stop - a first will do
    // the job
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
    // saveReadyCacheInContainerContext(ExoContainerContext.getCurrentContainer().getContext().getName(),
    // false);
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
   * Aggregate the adding focus into the tracked one.
   *
   * @param tracked the tracked focus entity
   * @param add the adding focus entity
   */
  protected void agregateFocus(ActivityFocusEntity tracked, ActivityFocusEntity add) {
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
   * @throws ActivityStatsException the smart activity exception
   */
  @ExoTransactional
  protected void saveActivityFocus(ActivityFocusEntity focus) throws ActivityStatsException {
    //
    if (LOG.isDebugEnabled()) {
      LOG.debug(">> saveActivityFocus: " + focus);
    }
    try {
      FocusId fid = focus.getId();
      long startTimeAfter = System.currentTimeMillis() - ActivityFocusTracker.BATCH_LIFETIME;
      if (startTimeAfter <= 0) {
        startTimeAfter = fid.getStartTime();
      }
      List<ActivityFocusEntity> trackedAfter = focusStorage.findFocusAfter(fid.getUserId(), fid.getActivityId(), startTimeAfter);
      ActivityFocusEntity tracked;
      if (trackedAfter.size() > 0) {
        tracked = trackedAfter.get(0);
      } else {
        tracked = null;
      }
      // ActivityFocusEntity tracked = focusStorage.find(focus.getId());
      if (tracked == null) {
        focusStorage.create(focus);
        if (LOG.isDebugEnabled()) {
          LOG.debug("<< saveActivityFocus => created: " + focus);
        }
      } else {
        // XXX This should not happen in normal circumstances as batch lifetime, which
        // also used by tracker.isReady(),
        // used above to calc startTimeAfter and persistent tracked focus have to be
        // older of it.
        // We replace if of same version
        if (tracked.getTrackerVersion().equals(focus.getTrackerVersion())) {
          agregateFocus(tracked, focus);
          focusStorage.update(tracked);
          if (LOG.isDebugEnabled()) {
            LOG.debug("<< saveActivityFocus => updated: " + tracked);
          }
        } else {
          LOG.warn("Cannot update activity focus of different tracker versions: " + tracked.getTrackerVersion() + " vs "
              + focus.getTrackerVersion());
          throw new ActivityStatsException("Cannot update activity focus of different tracker versions");
        }
      }
    } catch (PersistenceException e) {
      LOG.error("Failed to save activity focus {}:{}", focus.getUserId(), focus.getActivityId(), e);
      throw new ActivityStatsException("Failed to save activity focus", e);
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
            // If an error will happen during the save, we will keep the focus for a next
            // attempt,
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
   * Save cached trackers in the container context.
   *
   * @param exoContainer the exo container
   * @param readyOnly if <code>true</code> then will save ready only trackers
   */
  protected void saveCacheInContainerContext(final ExoContainer exoContainer, final boolean readyOnly) {
    // Do the work under eXo container context (for proper work of eXo apps and JPA
    // storage)
    // ExoContainer exoContainer =
    // ExoContainerContext.getContainerByName(containerName);
    if (exoContainer != null) {
      final ExoContainer contextContainer = ExoContainerContext.getCurrentContainerIfPresent();
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
      LOG.warn("Container not found " + exoContainer + " for saving trackers");
    }
  }

  /**
   * Stop tracker.
   *
   * @param container the container
   */
  private void stopTracker(ExoContainer container) {
    if (focusSaverStarted.getAndSet(false)) {
      focusSaver.cancel();
      saveCacheInContainerContext(container, false);
    }
  }

}
