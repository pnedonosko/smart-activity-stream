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
import org.exoplatform.smartactivitystream.stats.domain.UserFocus;
import org.exoplatform.social.core.manager.ActivityManager;

import java.util.List;

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
   * @return user focuses (rows of the parent table)
   */
  @GET
  @RolesAllowed("users")
  @Path("/userfocus/{streamSelected}/{substreamSelected}")
  public Response getUserFocuses(@Context UriInfo uriInfo,
                                 @Context HttpServletRequest request,
                                 @PathParam("streamSelected") String streamSelected,
                                 @PathParam("substreamSelected") String substreamSelected) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">> getUserFocuses");
    }

    LOG.info("streamSelected: " + streamSelected);
    LOG.info("substreamSelected: " + substreamSelected);

    ActivityManager activityManager = activityStatsService.getActivityManager();

    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String currentUserId = convo.getIdentity().getUserId();

      List<UserFocus> userFocuses = activityStatsService.getUserFocuses(streamSelected, substreamSelected, currentUserId);

      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getUserFocuses");
      }
      return Response.status(Status.ACCEPTED).entity(userFocuses.toArray()).build();
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getUserFocuses conversationState == null");
      }
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }
}
