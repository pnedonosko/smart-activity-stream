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
package org.exoplatform.smartactivitystream.cometd;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.cometd.annotation.Param;
import org.cometd.annotation.ServerAnnotationProcessor;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.annotation.Subscription;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.eclipse.jetty.util.component.LifeCycle;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;
import org.picocontainer.Startable;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.SmartActivityException;
import org.exoplatform.smartactivitystream.SmartActivityService;
import org.exoplatform.smartactivitystream.command.CommandThreadFactory;
import org.exoplatform.smartactivitystream.command.ContainerCommand;
import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusEntity;

/**
 * The CometdSmartActivityService.
 */
public class CometdSmartActivityService implements Startable {

  /** The Constant LOG. */
  private static final Log              LOG                    = ExoLogger.getLogger(CometdSmartActivityService.class);

  /** The channel name. */
  public static final String            USERFOCUS_CHANNEL_NAME = "/eXo/Application/SmartActivity/userfocus/{userId}";

  /**
   * Base minimum number of threads for document updates thread executors.
   */
  public static final int               MIN_THREADS            = 2;

  /**
   * Minimal number of threads maximum possible for thread executors.
   */
  public static final int               MIN_MAX_THREADS        = 4;

  /** Thread idle time for thread executors (in seconds). */
  public static final int               THREAD_IDLE_TIME       = 120;

  /**
   * Maximum threads per CPU for thread executors of document changes channel.
   */
  public static final int               MAX_FACTOR             = 100;

  /**
   * Queue size per CPU for thread executors of document updates channel.
   */
  public static final int               QUEUE_FACTOR           = MAX_FACTOR * 2;

  /**
   * Thread name used for the executor.
   */
  public static final String            THREAD_PREFIX          = "smartactivity-comet-thread-";

  protected final SmartActivityService  smartActivityService;

  /** The exo bayeux. */
  protected final EXoContinuationBayeux exoBayeux;

  /** The service. */
  protected final CometdService         service;

  /** The call handlers. */
  protected final ExecutorService       eventsHandlers;

  /**
   * Instantiates the CometdSmartActivityService.
   *
   * @param exoBayeux the exoBayeux
   */
  public CometdSmartActivityService(EXoContinuationBayeux exoBayeux, SmartActivityService smartActivityService) {
    this.exoBayeux = exoBayeux;
    this.smartActivityService = smartActivityService;
    this.service = new CometdService();
    this.eventsHandlers = createThreadExecutor(THREAD_PREFIX, MAX_FACTOR, QUEUE_FACTOR);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
    // instantiate processor after the eXo container start, to let
    // start-dependent logic worked before us
    final AtomicReference<ServerAnnotationProcessor> processor = new AtomicReference<>();
    // need initiate process after Bayeux server starts
    exoBayeux.addLifeCycleListener(new LifeCycle.Listener() {
      @Override
      public void lifeCycleStarted(LifeCycle event) {
        ServerAnnotationProcessor p = new ServerAnnotationProcessor(exoBayeux);
        processor.set(p);
        p.process(service);
      }

      @Override
      public void lifeCycleStopped(LifeCycle event) {
        ServerAnnotationProcessor p = processor.get();
        if (p != null) {
          p.deprocess(service);
        }
      }

      @Override
      public void lifeCycleStarting(LifeCycle event) {
        // Nothing
      }

      @Override
      public void lifeCycleFailure(LifeCycle event, Throwable cause) {
        // Nothing
      }

      @Override
      public void lifeCycleStopping(LifeCycle event) {
        // Nothing
      }
    });

    if (PropertyManager.isDevelopping()) {
      // This listener not required for work, just for info during development
      exoBayeux.addListener(new BayeuxServer.SessionListener() {
        @Override
        public void sessionRemoved(ServerSession session, boolean timedout) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("sessionRemoved: " + session.getId() + " timedout:" + timedout + " channels: "
                + channelsAsString(session.getSubscriptions()));
          }
        }

        @Override
        public void sessionAdded(ServerSession session, ServerMessage message) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("sessionAdded: " + session.getId() + " channels: " + channelsAsString(session.getSubscriptions()));
          }
        }
      });
    }
  }

  /**
   * The CometService is responsible for sending messages to Cometd channels.
   */
  @Service("smartactivity")
  public class CometdService {

    /** The bayeux. */
    @Inject
    private BayeuxServer  bayeux;

    /** The local session. */
    @Session
    private LocalSession  localSession;

    /** The server session. */
    @Session
    private ServerSession serverSession;

    /**
     * Post construct.
     */
    @PostConstruct
    public void postConstruct() {
      // TODO ?
    }

    /**
     * Subscribe for userfocus messages.
     *
     * @param message the message.
     * @param userId the user id
     * @throws RepositoryException the repository exception
     */
    @Subscription(USERFOCUS_CHANNEL_NAME)
    public void subscribeUserfocus(Message message, @Param("userId") String userId) throws RepositoryException {
      Object objData = message.getData();
      if (!Map.class.isInstance(objData)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Couldn't get data as a map from event");
        }
        return;
      }

      Map<String, Object> data = message.getDataAsMap();
      saveUserfocus(data);

//      switch (type) {
//      case DOCUMENT_CHANGED_EVENT:
//        handleDocumentChangeEvent(data, docId);
//        break;
//      case DOCUMENT_VERSION_EVENT:
//        handleDocumentVersionEvent(data, docId);
//        break;
//      case DOCUMENT_LINK_EVENT:
//        handleDocumentLinkEvent(data, docId);
//        break;
//      case DOCUMENT_TITLE_UPDATED:
//        handleDocumentTitleUpdatedEvent(data, docId);
//        break;
//      case DOCUMENT_USERSAVED:
//        handleDocumentUsersavedEvent(data, docId);
//        break;
//      case EDITOR_CLOSED_EVENT:
//        handleEditorClosedEvent(data, docId);
//        break;
//      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("Event published in " + message.getChannel() + ", userId: " + userId + ", data: " + message.getJSON());
      }
    }

    /**
     * Save userfocus.
     * 
     * @param data the data
     * @param docId the docId
     */
    protected void saveUserfocus(Map<String, Object> data) {
      eventsHandlers.submit(new ContainerCommand(PortalContainer.getCurrentPortalContainerName()) {
        @Override
        protected void onContainerError(String error) {
          LOG.error("An error has occured in container: {}", containerName);
        }

        @Override
        protected void execute(ExoContainer exoContainer) {
          ActivityFocusEntity focus = new ActivityFocusEntity();
          focus.setUserId((String) data.get("userId"));
          focus.setActivityId((String) data.get("activityId"));
          focus.setStartTime((Long) data.get("startTime"));
          focus.setStopTime((Long) data.get("stopTime"));
          focus.setTotalShown((Long) data.get("totalShown"));
          focus.setContentShown((Long) data.get("contentShown"));
          focus.setConvoShown((Long) data.get("convoShown"));
          focus.setContentHits((Long) data.get("contentHits"));
          focus.setConvoHits((Long) data.get("convoHits"));
          focus.setAppHits((Long) data.get("appHits"));
          focus.setProfileHits((Long) data.get("profileHits"));
          focus.setLinkHits((Long) data.get("linkHits"));
          focus.setTrackerVersion(ActivityFocusEntity.TRACKER_VERSION);
          try {
            smartActivityService.submitActivityFocus(focus);
          } catch (SmartActivityException e) {
            // It's already logged in the service method
          }
        }
      });
    }
  }

  /**
   * Channels as string.
   *
   * @param channels the channels
   * @return the string
   */
  protected String channelsAsString(Set<ServerChannel> channels) {
    return channels.stream().map(c -> c.getId()).collect(Collectors.joining(", "));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    // Nothing
  }

  /**
   * Gets the cometd server path.
   *
   * @return the cometd server path
   */
  public String getCometdServerPath() {
    return new StringBuilder("/").append(exoBayeux.getCometdContextName()).append("/cometd").toString();
  }

  /**
   * Gets the user token.
   *
   * @param userId the userId
   * @return the token
   */
  public String getUserToken(String userId) {
    return exoBayeux.getUserToken(userId);
  }

  /**
   * Create a new thread executor service.
   *
   * @param threadNamePrefix the thread name prefix
   * @param maxFactor - max processes per CPU core
   * @param queueFactor - queue size per CPU core
   * @return the executor service
   */
  protected ExecutorService createThreadExecutor(String threadNamePrefix, int maxFactor, int queueFactor) {
    // Executor will queue all commands and run them in maximum set of threads.
    // Minimum set of threads will be
    // maintained online even idle, other inactive will be stopped in two
    // minutes.
    final int cpus = Runtime.getRuntime().availableProcessors();
    int poolThreads = cpus / 4;
    poolThreads = poolThreads < MIN_THREADS ? MIN_THREADS : poolThreads;
    int maxThreads = Math.round(cpus * 1f * maxFactor);
    maxThreads = maxThreads > 0 ? maxThreads : 1;
    maxThreads = maxThreads < MIN_MAX_THREADS ? MIN_MAX_THREADS : maxThreads;
    int queueSize = cpus * queueFactor;
    queueSize = queueSize < queueFactor ? queueFactor : queueSize;
    if (LOG.isDebugEnabled()) {
      LOG.debug("Creating thread executor " + threadNamePrefix + "* for " + poolThreads + ".." + maxThreads
          + " threads, queue size " + queueSize);
    }
    return new ThreadPoolExecutor(poolThreads,
                                  maxThreads,
                                  THREAD_IDLE_TIME,
                                  TimeUnit.SECONDS,
                                  new LinkedBlockingQueue<Runnable>(queueSize),
                                  new CommandThreadFactory(threadNamePrefix),
                                  new ThreadPoolExecutor.CallerRunsPolicy());
  }

}
