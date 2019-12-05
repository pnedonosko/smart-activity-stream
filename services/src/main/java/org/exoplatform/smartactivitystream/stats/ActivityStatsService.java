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
import org.exoplatform.smartactivitystream.stats.settings.GlobalSettings;
import org.exoplatform.smartactivitystream.stats.dao.ActivityStatsDAO;
import org.exoplatform.smartactivitystream.stats.domain.ActivityStatsEntity;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.exoplatform.social.core.service.LinkProvider;
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

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: SmartActivityService.java 00000 Oct 2, 2019 pnedonosko $
 */
public class ActivityStatsService implements Startable {

  /** The Constant LOG. */
  private static final Log                             LOG                      = ExoLogger.getLogger(ActivityStatsService.class);

  /** The Constant TRACKER_CACHE_NAME. */
  public static final String                           TRACKER_CACHE_NAME       = "smartactivity.TrackerCache".intern();

  /** The Constant TRACKER_CACHE_PERIOD. */
  public static final int                              TRACKER_CACHE_PERIOD     = 120000;

  /** The Constant ACTIVITY_FOCUS_DEFAULT. */
  private static final long                            ACTIVITY_FOCUS_DEFAULT   = 0;

  /** The Constant Stream Selector ALL_STREAMS. */
  public static final String                           ALL_STREAMS              = "All streams";

  /** The Constant Stream Selector SPACE. */
  public static final String                           SPACE                    = "Space";

  /** The Constant Stream Selector USER. */
  public static final String                           USER                     = "User";

  /** The Constant Substream Selector ALL_USERS. */
  public static final String                           ALL_USERS                = "All users";

  /** The Constant Substream Selector ALL_SPACES. */
  public static final String                           ALL_SPACES               = "All spaces";

  /** The text policy. */
  protected final PolicyFactory                        textPolicy               = new HtmlPolicyBuilder().toFactory();

  /** The link with href not a hash in local document target. */
  protected final Pattern                              linkNotLocal             =
                                                                    Pattern.compile("href=['\"][^#][.\\w\\W\\S]*?['\"]",
                                                                                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                                                                                        | Pattern.DOTALL);

  /** The link with target. */
  protected final Pattern                              linkWithTarget           =
                                                                      Pattern.compile("<a(?=\\s).*?(target=['\"].*?['\"])[^>]*>",
                                                                                      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                                                                                          | Pattern.DOTALL);

  /** The link without target. */
  protected final Pattern                              linkWithoutTarget        =
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
  private final Timer                                  focusSaver               = new Timer();

  /** The focus saver started. */
  private final AtomicBoolean                          focusSaverStarted        = new AtomicBoolean(false);

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

  /** The global settings. */
  private GlobalSettings                               configuredGlobalSettings = new GlobalSettings();

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

  /**
   * Gets the maxTotalShown.
   *
   * @return the maxTotalShown
   */
  public Long getMaxTotalShown() {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">>> getMaxTotalShown");
    }

    Long maxTotalShown = statsStorage.findMaxTotalShown();

    if (LOG.isDebugEnabled()) {
      LOG.debug("<<< getMaxTotalShown");
    }
    return maxTotalShown;
  }

  /**
   * Gets global settings.
   *
   * @return configured global settings
   */
  public GlobalSettings getSettings() {
    return this.configuredGlobalSettings.clone();
  }

  /**
   * Gets the data for the subtable.
   * 
   * @param activityId the selected activity of the table
   * @param timeScale the time scaling
   * @param userLocale the user locale
   * @return list of the activity subtable (user focuses on selected activity)
   */
  public List<ActivityStatsEntity> getActivityFocuses(String activityId, String timeScale, Locale userLocale) {

    if (LOG.isDebugEnabled()) {
      LOG.debug(">>> getActivityFocuses");
    }

    List<ActivityStatsEntity> activityFocuses = statsStorage.findActivityFocuses(activityId, timeScale);

    for (ActivityStatsEntity activityFocus : activityFocuses) {
      activityFocus.setUserLocale(userLocale);
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("<<< getActivityFocuses");
    }

    return activityFocuses;
  }

  /**
   * Finds the activity stats (the sum of activity statistics by components
   * grouped by activity id).
   *
   * @param activityId the activity
   * @return the activity stats entity
   */
  public ActivityStatsEntity findActivityStats(String activityId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">>> findActivityStats");
    }

    ActivityStatsEntity activityStatsRecord = statsStorage.findActivityStats(activityId);

    if (LOG.isDebugEnabled()) {
      LOG.debug("<<< findActivityStats");
    }

    return activityStatsRecord;
  }

  /**
   * Gets the user identity.
   *
   * @param userId the user id
   * @return the activity stats entity
   */
  public Identity getUserIdentity(String userId) {
    return identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);
  }

  /**
   * Gets the user spaces.
   *
   * @param userId the user id
   * @return the list of user spaces
   */
  public List<Space> getUserSpaces(String userId) {
    return spaceStorage.getMemberSpaces(userId);
  }

  /**
   * Gets the user connections.
   *
   * @param userId the user id
   * @return the list of user identities
   */
  public List<Identity> getUserConnections(String userId) {
    return relationshipStorage.getConnections(getUserIdentity(userId));
  }

  /**
   * Gets the user activities focuses.
   *
   * @param stream the user id
   * @param substream the user id
   * @param currentUserId the user id
   * @param userLocale the user id
   * @return the list of activity stats entities
   */
  public List<ActivityStatsEntity> getUserActivitiesFocuses(String stream,
                                                            String substream,
                                                            String currentUserId,
                                                            Locale userLocale) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">>> getUserActivitiesFocuses");
    }

    List<ActivityStatsEntity> activityStatsEntities = new LinkedList<>();

    if (stream != null) {
      Identity userIdentity = getUserIdentity(currentUserId);

      if (userIdentity != null) {
        this.userLocale = userLocale;

        switch (stream) {
        case ALL_STREAMS: {
          RealtimeListAccess<ExoSocialActivity> usersActivities = activityManager.getActivityFeedWithListAccess(userIdentity);
          List<ExoSocialActivity> allUserActivities = usersActivities.loadAsList(0, usersActivities.getSize());
          if (LOG.isDebugEnabled()) {
            LOG.debug("Stream selector: All streams");
          }

          for (ExoSocialActivity exoSocialActivity : allUserActivities) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("activity selected:" + exoSocialActivity.getName());
            }

            addActivityToUserFocuses(exoSocialActivity, activityStatsEntities);
          }
          break;
        }
        case SPACE: {
          RealtimeListAccess<ExoSocialActivity> usersActivities =
                                                                activityManager.getActivitiesOfUserSpacesWithListAccess(userIdentity);
          List<ExoSocialActivity> allUserActivities = usersActivities.loadAsList(0, usersActivities.getSize());
          if (LOG.isDebugEnabled()) {
            LOG.debug("Stream selector: Space");
          }

          addActivitiesFromUserSpaceToUserFocuses(allUserActivities, substream, activityStatsEntities);
          break;
        }
        case USER: {
          RealtimeListAccess<ExoSocialActivity> usersActivities =
                                                                activityManager.getActivitiesOfConnectionsWithListAccess(userIdentity);
          List<ExoSocialActivity> allUserActivities = usersActivities.loadAsList(0, usersActivities.getSize());
          if (LOG.isDebugEnabled()) {
            LOG.debug("Stream selector: User");
          }

          addActivitiesFromUserConnectionsToUserFocuses(allUserActivities, substream, activityStatsEntities);
          break;
        }
        }
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("<<< getUserActivitiesFocuses");
    }

    return activityStatsEntities;
  }

  /**
   * Adds the activity stats to the activityStatsEntities list.
   *
   * @param exoSocialActivity the exo social activity
   * @param activityStatsEntities the list of activity stats entities
   */
  private void addActivityToUserFocuses(ExoSocialActivity exoSocialActivity, List<ActivityStatsEntity> activityStatsEntities) {
    String exoSocialActivityId = exoSocialActivity.getId();
    ActivityStatsEntity activityStatsEntity = findActivityStats(exoSocialActivityId);

    if (activityStatsEntity != null) {
      // Creates the safe activity title
      String safeActivityTitle = safeText(exoSocialActivity.getTitle());

      // Limits the activity title (40 characters)
      if (safeActivityTitle.length() > 40) {
        safeActivityTitle = safeActivityTitle.substring(0, 40);
      }

      ActivityStream activityStream = exoSocialActivity.getActivityStream();

      activityStatsEntity.setActivityTitle(safeActivityTitle);

      activityStatsEntity.setActivityCreatedMilliseconds(exoSocialActivity.getPostedTime());

      activityStatsEntity.setActivityUpdatedMilliseconds(exoSocialActivity.getUpdated().getTime());

      activityStatsEntity.setActivityStreamPrettyId(activityStream.getPrettyId());

      // Defines a data for charts of the main table
      activityStatsEntity.setFocusChartData(statsStorage.findActivityFocusChartData(exoSocialActivityId)
                                                        .toArray(new String[0][]));

      activityStatsEntity.setActivityUrl(LinkProvider.getSingleActivityUrl(exoSocialActivity.getId()));

      // Sets the locale and localizes time variables
      activityStatsEntity.setUserLocale(userLocale);

      activityStatsEntities.add(activityStatsEntity);
    }
  }

  /**
   * Adds activities from the user space to the activityStatsEntities list.
   *
   * @param allUserActivities all user activities
   * @param substreamSelected the substream selected
   * @param activityStatsEntities the list of activity stats entities
   */
  private void addActivitiesFromUserSpaceToUserFocuses(List<ExoSocialActivity> allUserActivities,
                                                       String substreamSelected,
                                                       List<ActivityStatsEntity> activityStatsEntities) {
    if (substreamSelected != null) {
      if (ALL_SPACES.equals(substreamSelected)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(ALL_SPACES);
        }

        for (ExoSocialActivity exoSocialActivity : allUserActivities) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("activity selected:" + exoSocialActivity.getName());
          }

          addActivityToUserFocuses(exoSocialActivity, activityStatsEntities);
        }
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("space: " + substreamSelected);
        }

        for (ExoSocialActivity exoSocialActivity : allUserActivities) {
          if (substreamSelected.equals(exoSocialActivity.getActivityStream().getPrettyId())) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("activity selected:" + exoSocialActivity.getName());
            }

            addActivityToUserFocuses(exoSocialActivity, activityStatsEntities);
          }
        }
      }
    }
  }

  /**
   * Adds activities from the user connections to the activityStatsEntities list.
   *
   * @param allUserActivities all user activities
   * @param substreamSelected the substream selected
   * @param activityStatsEntities the list of activity stats entities
   */
  private void addActivitiesFromUserConnectionsToUserFocuses(List<ExoSocialActivity> allUserActivities,
                                                             String substreamSelected,
                                                             List<ActivityStatsEntity> activityStatsEntities) {
    if (substreamSelected != null) {
      if (ALL_USERS.equals(substreamSelected)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(ALL_USERS);
        }

        for (ExoSocialActivity exoSocialActivity : allUserActivities) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("activity selected:" + exoSocialActivity.getName());
          }
          addActivityToUserFocuses(exoSocialActivity, activityStatsEntities);
        }
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("user: " + substreamSelected);
        }

        for (ExoSocialActivity exoSocialActivity : allUserActivities) {
          if (substreamSelected.equals(exoSocialActivity.getUserId())) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("activity selected:" + exoSocialActivity.getName());
            }

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
      long startTimeAfter = System.currentTimeMillis() - ActivityFocusTracker.BATCH_LIFETIME;
      if (startTimeAfter <= 0) {
        startTimeAfter = focus.getStartTime();
      }
      List<ActivityFocusEntity> trackedAfter = focusStorage.findFocusAfter(focus.getUserId(),
                                                                           focus.getActivityId(),
                                                                           startTimeAfter);
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
