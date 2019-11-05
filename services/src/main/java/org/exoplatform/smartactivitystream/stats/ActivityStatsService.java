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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tika.parser.microsoft.OutlookExtractor;
import org.exoplatform.smartactivitystream.stats.dao.ActivityStatsDAO;
import org.exoplatform.smartactivitystream.stats.domain.ActivityStatsEntity;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.owasp.html.PolicyFactory;
import org.owasp.html.HtmlPolicyBuilder;
import org.picocontainer.Startable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusId;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: SmartActivityService.java 00000 Oct 2, 2019 pnedonosko $
 */
public class ActivityStatsService implements Startable {

  /** The Constant LOG. */
  private static final Log                             LOG                    = ExoLogger.getLogger(ActivityStatsService.class);

  /** The Constant TRACKER_CACHE_NAME. */
  public static final String                           TRACKER_CACHE_NAME     = "smartactivity.TrackerCache".intern();

  /** The Constant TRACKER_CACHE_PERIOD. */
  public static final int                              TRACKER_CACHE_PERIOD   = 120000;

  /** The Constant ACTIVITY_FOCUS_DEFAULT. */
  private static final long                            ACTIVITY_FOCUS_DEFAULT = 0;

  /** The text policy. */
  protected final PolicyFactory                        textPolicy             = new HtmlPolicyBuilder().toFactory();

  /** The link with href not a hash in local document target. */
  protected final Pattern                              linkNotLocal           =
                                                                    Pattern.compile("href=['\"][^#][.\\w\\W\\S]*?['\"]",
                                                                                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                                                                                        | Pattern.DOTALL);

  /** The link with target. */
  protected final Pattern                              linkWithTarget         =
                                                                      Pattern.compile("<a(?=\\s).*?(target=['\"].*?['\"])[^>]*>",
                                                                                      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                                                                                          | Pattern.DOTALL);

  /** The link without target. */
  protected final Pattern                              linkWithoutTarget      =
                                                                         Pattern.compile("<a(?=\\s)(?:(?!target=).)*?([.\\W\\w\\S\\s[^>]])*?(>)",
                                                                                         Pattern.CASE_INSENSITIVE
                                                                                             | Pattern.MULTILINE
                                                                                             | Pattern.DOTALL);

  /** Cache of tracking activities. */
  private final ExoCache<String, ActivityFocusTracker> trackerCache;

  /** The focus storage. */
  private final ActivityFocusDAO                       focusStorage;

  /** The stats storage. */
  private final ActivityStatsDAO                       statsStorage;

  /** The focus saver. */
  private final Timer                                  focusSaver             = new Timer();

  /** The focus saver started. */
  private final AtomicBoolean                          focusSaverStarted      = new AtomicBoolean(false);

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

  /** The user storage. */
  private Locale                                       userLocale;

  /**
   * Instantiates a new smart activity service.
   *
   * @param focusStorage the focus storage
   * @param statsStorage the stats storage
   * @param cacheService the cache service
   * @param params the params
   */
  public ActivityStatsService(ActivityFocusDAO focusStorage,
                              ActivityStatsDAO statsStorage,
                              CacheService cacheService,
                              InitParams params,
                              ActivityManager activityManager,
                              IdentityManagerImpl identityManager,
                              IdentityProvider identityProvider,
                              SpaceStorage spaceStorage,
                              RelationshipStorage relationshipStorage) {
    this.focusStorage = focusStorage;
    this.statsStorage = statsStorage;
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

  /*
   * I (Nick Riabovol) will change it (I'm going to get a data for the subtable
   * from this method)
   */
  public ActivityFocusEntity findActivityFocus(String activityId) {
    ActivityFocusEntity activityFocusEntity = null;
    List<ActivityFocusEntity> activityFocusRecords = null;

    LOG.info("findActivityFocus start");
    activityFocusRecords = focusStorage.findActivityFocus(activityId);

    if (activityFocusRecords.size() > 0) {
      LOG.info("findActivityFocus activityFocusRecords.size>0:");
      activityFocusEntity = activityFocusRecords.get(0);
      LOG.info("findActivityFocus finished successfully");
    }

    return activityFocusEntity;
  }

  public ActivityStatsEntity findActivityStats(String activityId) {
    ActivityStatsEntity activityStatsEntity = null;

    LOG.info("findActivityStats start");
    List<ActivityStatsEntity> activityStatsRecords = activityStatsRecords = statsStorage.findActivityStats(activityId);

    if (activityStatsRecords.size() > 0) {
      LOG.info("findActivityStats activityFocusRecords.size>0, size:" + activityStatsRecords.size());
      activityStatsEntity = activityStatsRecords.get(0);
      LOG.info("activityStatsEntity:" + activityStatsEntity.toString());
      LOG.info("findActivityStats finished successfully");
    }

    return activityStatsEntity;
  }

  public Identity getUserIdentity(String userId) {
    return identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);
  }

  public List<Space> getUserSpaces(String userId) {
    return spaceStorage.getMemberSpaces(userId);
  }

  public List<Identity> getUserConnections(String userId) {
    return relationshipStorage.getConnections(getUserIdentity(userId));
  }

  public List<ActivityStatsEntity> getActivityFocuses(String stream, String substream, String currentUserId, Locale userLocale) {
    List<ActivityStatsEntity> activityStatsEntities = new LinkedList<>();

    if (stream != null) {
      Identity userIdentity = getUserIdentity(currentUserId);

      if (userIdentity != null) {
        this.userLocale = userLocale;

        switch (stream) {
        case "All streams": {
          RealtimeListAccess<ExoSocialActivity> usersActivities = activityManager.getActivityFeedWithListAccess(userIdentity);
          List<ExoSocialActivity> allUserActivities = usersActivities.loadAsList(0, usersActivities.getSize());
          LOG.info("All streams");
          for (ExoSocialActivity exoSocialActivity : allUserActivities) {
            LOG.info("All streams add");
            addActivityToUserFocuses(exoSocialActivity, activityStatsEntities);
          }
          break;
        }
        case "Space": {
          RealtimeListAccess<ExoSocialActivity> usersActivities =
                                                                activityManager.getActivitiesOfUserSpacesWithListAccess(userIdentity);
          List<ExoSocialActivity> allUserActivities = usersActivities.loadAsList(0, usersActivities.getSize());
          LOG.info("Space");
          addActivitiesFromUserSpaceToUserFocuses(allUserActivities, substream, activityStatsEntities);
          break;
        }
        case "User": {
          RealtimeListAccess<ExoSocialActivity> usersActivities =
                                                                activityManager.getActivitiesOfConnectionsWithListAccess(userIdentity);
          List<ExoSocialActivity> allUserActivities = usersActivities.loadAsList(0, usersActivities.getSize());
          LOG.info("User");
          addActivitiesFromUserConnectionsToUserFocuses(allUserActivities, substream, activityStatsEntities);
          break;
        }
        }

      }
    }
    return activityStatsEntities;
  }

  private void addActivityToUserFocuses(ExoSocialActivity exoSocialActivity, List<ActivityStatsEntity> activityStatsEntities) {
    ActivityStatsEntity activityStatsEntity = findActivityStats(exoSocialActivity.getId());

    String safeActivityTitle = safeText(exoSocialActivity.getTitle());

    if (activityStatsEntity != null) {
      activityStatsEntity.setActivityTitle(safeActivityTitle);

      activityStatsEntity.setActivityCreatedMilliseconds(exoSocialActivity.getPostedTime());

      activityStatsEntity.setActivityUpdatedMilliseconds(exoSocialActivity.getUpdated().getTime());

      activityStatsEntity.setUserLocale(userLocale);

    } else {
      activityStatsEntity = new ActivityStatsEntity(safeActivityTitle,
                                                    exoSocialActivity.getPostedTime(),
                                                    exoSocialActivity.getUpdated().getTime(),
                                                    exoSocialActivity.getId(),
                                                    userLocale);
    }

    activityStatsEntities.add(activityStatsEntity);
  }

  private void addActivitiesFromUserSpaceToUserFocuses(List<ExoSocialActivity> allUserActivities,
                                                       String substreamSelected,
                                                       List<ActivityStatsEntity> activityStatsEntities) {
    if (substreamSelected != null) {
      if ("All spaces".equals(substreamSelected)) {
        LOG.info("All spaces");
        for (ExoSocialActivity exoSocialActivity : allUserActivities) {
          LOG.info("All spaces add");
          addActivityToUserFocuses(exoSocialActivity, activityStatsEntities);
        }
      } else {
        LOG.info("space " + substreamSelected);
        for (ExoSocialActivity exoSocialActivity : allUserActivities) {
          if (substreamSelected.equals(exoSocialActivity.getStreamId())) {
            LOG.info("space add");
            addActivityToUserFocuses(exoSocialActivity, activityStatsEntities);
          }
        }
      }
    }
  }

  private void addActivitiesFromUserConnectionsToUserFocuses(List<ExoSocialActivity> allUserActivities,
                                                             String substreamSelected,
                                                             List<ActivityStatsEntity> activityStatsEntities) {
    if (substreamSelected != null) {
      if ("All users".equals(substreamSelected)) {
        LOG.info("All users");

        for (ExoSocialActivity exoSocialActivity : allUserActivities) {
          LOG.info("All users add");
          addActivityToUserFocuses(exoSocialActivity, activityStatsEntities);
        }
      } else {
        LOG.info("user " + substreamSelected);
        for (ExoSocialActivity exoSocialActivity : allUserActivities) {
          if (substreamSelected.equals(exoSocialActivity.getUserId())) {
            LOG.info("user add");
            addActivityToUserFocuses(exoSocialActivity, activityStatsEntities);
          }
        }
      }
    }
  }

  /**
   * Allow only plain text.
   *
   * @param content {@link String}
   * @return {@link String} sanitized content (as plain text)
   */
  protected String safeText(String content) {
    String safe = textPolicy.sanitize(content);
    safe = makeLinksOpenNewWindow(safe);
    safe = StringEscapeUtils.unescapeHtml(safe);
    return safe;
  }

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
      ActivityFocusId fid = focus.getId();
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

  /**
   * Make links open new window.
   *
   * @param text the text
   * @return the string
   */
  protected String makeLinksOpenNewWindow(String text) {
    // Make all links target a new window
    // Replace in all links with target attribute to its _blank value
    Matcher m = linkWithTarget.matcher(text);
    StringBuilder sb = new StringBuilder();
    int pos = 0;
    while (m.find()) {
      if (linkNotLocal.matcher(m.group()).find()) {
        int start = m.start(1);
        int end = m.end(1);
        if (start >= 0 && end >= 0) {
          sb.append(text.substring(pos, start));
          sb.append("target=\"_blank\"");
          pos = end;
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Cannot find link target group in " + m.group(1));
          }
        }
      }
    }
    if (pos < text.length()) {
      sb.append(text.substring(pos));
    }
    text = sb.toString();

    // Add in all links without target attribute add it with _blank value
    m = linkWithoutTarget.matcher(text);
    sb = new StringBuilder();
    pos = 0;
    while (m.find()) {
      if (linkNotLocal.matcher(m.group()).find()) {
        int start = m.start(2);
        int end = m.end(2);
        if (start >= 0 && end >= 0) {
          sb.append(text.substring(pos, start));
          sb.append(" target=\"_blank\"");
          sb.append(text.substring(start, end));
          pos = end;
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Cannot find link end group in " + m.group(2));
          }
        }
      }
    }
    if (pos < text.length()) {
      sb.append(text.substring(pos));
    }
    return sb.toString();
  }

}
