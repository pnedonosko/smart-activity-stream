/*
 * 
 */
package org.exoplatform.smartactivitystream.stats.rest;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.smartactivitystream.stats.ActivityStatsService;
import org.exoplatform.smartactivitystream.stats.domain.ChartPoint;
import org.exoplatform.smartactivitystream.stats.domain.UserActivityStats;
import org.exoplatform.smartactivitystream.stats.domain.ActivityStatsEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The REST service for Activity Statistics Service.
 */
@Path("/smartactivity/stats")
@Produces(MediaType.APPLICATION_JSON)
public class RESTActivityStatsService implements ResourceContainer {

  /** The Constant LOG. */
  protected static final Log           LOG = ExoLogger.getLogger(RESTActivityStatsService.class);

  /** The Activity Stats Service service. */
  protected final ActivityStatsService activityStatsService;

  /**
   * Instantiates a new REST activity stats service.
   *
   * @param activityStatsService the activity stats service
   */
  public RESTActivityStatsService(ActivityStatsService activityStatsService) {
    this.activityStatsService = activityStatsService;
  }

  /**
   * Saves the cached stats to database.
   *
   * @return the response
   */
  @GET
  @RolesAllowed("users")
  @Path("/save")
  @Produces("application/json")
  public Response saveStats() {
    try {
      if (activityStatsService.isTrackersEnabled()) {
        activityStatsService.saveTrackers();
        return Response.status(Status.OK).entity("{ \"success\" : \"Saved\", \"message\" : \"Trackers saved\" }").build();
      } else {
        return Response.status(Status.BAD_REQUEST)
                       .entity("{ \"error\" : \"Not enabled\", \"message\" : \"Trackers not enabled\" }")
                       .build();
      }
    } catch (Throwable e) {
      LOG.error("Error saving activity statistics: ", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{ \"error\" : \"Internal error\", \"message\" : \"" + e.getMessage() + "\" }")
                     .build();
    }
  }

  /**
   * Gets user focuses data (table tow).
   *
   * @param uriInfo the uri info
   * @param request the request
   * @param stream the stream
   * @param substream the substream
   * @return user focuses (rows of the parent table)
   */
  @GET
  @RolesAllowed("users")
  @Path("/userfocus/{stream}/{substream}")
  public Response getUserActivitiesFocuses(@Context UriInfo uriInfo,
                                           @Context HttpServletRequest request,
                                           @PathParam("stream") String stream,
                                           @PathParam("substream") String substream) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">> getUserActivitiesFocuses");

      LOG.debug("stream: " + stream);
      LOG.debug("substream: " + substream);
    }

    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String currentUserId = convo.getIdentity().getUserId();
      Locale userLocale = request.getLocale();

      List<ActivityStatsEntity> activityStatsEntities = null;

      try {
        activityStatsEntities = activityStatsService.getUserActivitiesFocuses(stream, substream, currentUserId, userLocale);
      } catch (Exception e) {
        LOG.error("Database exception, activityStatsEntities is null");
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{ \"error\" : \"Internal error\", \"message\" : \"" + e.getMessage() + "\" }")
                       .build();
      }

      Long maxTotalShown = null;
      try {
        maxTotalShown = activityStatsService.getMaxTotalShown();
      } catch (Exception e) {
        LOG.error("Error getting maxTotalShown value:", e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{ \"error\" : \"Internal error\", \"message\" : \"" + e.getMessage() + "\" }")
                       .build();
      }

      UserActivityStats userActivityStats = new UserActivityStats(activityStatsEntities, maxTotalShown);

      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getUserActivitiesFocuses");
      }
      return Response.status(Status.ACCEPTED).entity(userActivityStats).build();
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getUserActivitiesFocuses conversationState == null");
      }
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

  /**
   * Gets user focuses data (table tow).
   *
   * @param uriInfo the uri info
   * @param request the request
   * @param activityId the activity identifier
   * @param timeScale the time scaling
   * @return user focuses (rows of the parent table)
   */
  @GET
  @RolesAllowed("users")
  @Path("/activityfocuses/{activityId}/{timeScale}")
  public Response getActivityFocuses(@Context UriInfo uriInfo,
                                     @Context HttpServletRequest request,
                                     @PathParam("activityId") String activityId,
                                     @PathParam("timeScale") String timeScale) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">> getActivityFocuses");

      LOG.debug("activity: " + activityId);
      LOG.debug("timeScale: " + timeScale);
    }

    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      Locale userLocale = request.getLocale();

      List<ActivityStatsEntity> activityFocusEntities = null;
      try {
        activityFocusEntities = activityStatsService.getActivityFocuses(activityId, timeScale, userLocale);
      } catch (Exception e) {
        LOG.error("Error getting activityFocusEntities:", e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{ \"error\" : \"Internal error\", \"message\" : \"" + e.getMessage() + "\" }")
                       .build();
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getActivityFocuses");
      }
      return Response.status(Status.ACCEPTED).entity(activityFocusEntities.toArray()).build();
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getActivityFocuses conversationState == null");
      }
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

  /**
   * Gets user focuses data for the main table charts(table tows).
   *
   * @param uriInfo the uri info
   * @param request the request
   * @param activityId the activity identifier
   * @return user focuses (rows of the parent table)
   */
  @GET
  @RolesAllowed("users")
  @Path("/activityfocuses/chart/{activityId}")
  public Response getActivityFocusesForChart(@Context UriInfo uriInfo,
                                             @Context HttpServletRequest request,
                                             @PathParam("activityId") String activityId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">> getActivityFocusesForChart, activity: " + activityId);
    }

    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      Locale userLocale = request.getLocale();

      List<ChartPoint> chartPoints = null;
      try {
        chartPoints = activityStatsService.findActivityFocusChartData(activityId, userLocale);
      } catch (Exception e) {
        LOG.error("Error getting chartPoints:", e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{ \"error\" : \"Internal error\", \"message\" : \"" + e.getMessage() + "\" }")
                       .build();
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getActivityFocusesForChart");
      }
      return Response.status(Status.ACCEPTED).entity(chartPoints.toArray()).build();
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getActivityFocusesForChart conversationState == null");
      }
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }
}
