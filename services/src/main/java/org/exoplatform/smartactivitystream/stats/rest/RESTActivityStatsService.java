/*
 * 
 */
package org.exoplatform.smartactivitystream.stats.rest;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.smartactivitystream.Utils;
import org.exoplatform.smartactivitystream.stats.ActivityStatsService;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;

import java.util.LinkedList;
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
  @POST
  @RolesAllowed("users")
  @Path("/userfocus")
  public Response getUserFocuses(@Context UriInfo uriInfo, @Context HttpServletRequest request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">> getUserFocuses");
    }

    ActivityManager activityManager = activityStatsService.getActivityManager();

    /*List<String> activityIds = new LinkedList<>();
    activityIds.add("1");

    List<ExoSocialActivity> activities = activityManager.getActivities(activityIds);*/

    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String currentUserId = convo.getIdentity().getUserId();


      //List<ExoSocialActivity> selectedActivities = new LinkedList<>();

      Identity userIdentity = activityStatsService.getUserIdentity(currentUserId);

      RealtimeListAccess<ExoSocialActivity> usersActivities = activityManager.getActivitiesByPoster(userIdentity);


      List<ExoSocialActivity> allUsersActivities = usersActivities.loadAsList(0, usersActivities.getSize());

      /*
       * for(ExoSocialActivity
       * exoSocialActivity:usersActivities.loadAsList(0,usersActivities.getSize())){
       * if(exoSocialActivity.getType()) selectedActivities.add(exoSocialActivity); }
       */

      String jsonResponse = null;

      try {

        jsonResponse = Utils.asJSON(allUsersActivities.toArray());

      } catch (Throwable e) {
        LOG.error("getUserFocuses error", e);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getUserFocuses");
      }
      return Response.status(Status.ACCEPTED).entity(jsonResponse).build();
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getUserFocuses conversationState == null");
      }
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

  /**
   * Gets user spaces.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @return user spaces
   */
  @POST
  @RolesAllowed("users")
  @Path("/userspaces")
  public Response getUserSpaces(@Context UriInfo uriInfo, @Context HttpServletRequest request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">> getUserSpaces");
    }

    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String currentUserId = convo.getIdentity().getUserId();

      List<Space> userSpaces = activityStatsService.getUserSpaces(currentUserId);

      String jsonResponse = null;

      try {
        jsonResponse = Utils.asJSON(userSpaces.toArray());
      } catch (Throwable e) {
        LOG.error("getUserSpaces error", e);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getUserSpaces");
      }
      return Response.status(Status.ACCEPTED).entity(jsonResponse).build();
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getUserSpaces conversationState == null");
      }
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

  /**
   * Gets user connections.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @return user connections
   */
  @POST
  @RolesAllowed("users")
  @Path("/userconnections")
  public Response getUserConnections(@Context UriInfo uriInfo, @Context HttpServletRequest request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">> getUserConnections");
    }

    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String currentUserId = convo.getIdentity().getUserId();

      List<Identity> userConnections = activityStatsService.getUserConnections(currentUserId);

      String jsonResponse = null;

      try {
        jsonResponse = Utils.asJSON(userConnections.toArray());
      } catch (Throwable e) {
        LOG.error("getUserConnections error", e);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getUserConnections");
      }
      return Response.status(Status.ACCEPTED).entity(jsonResponse).build();
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getUserConnections conversationState == null");
      }
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

}
