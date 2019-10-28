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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.smartactivitystream.Utils;
import org.exoplatform.smartactivitystream.stats.ActivityStatsService;
import org.exoplatform.smartactivitystream.stats.dao.ActivityFocusDAO;
import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusEntity;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

import java.util.Arrays;
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
   * Gets the activities focuses joined data.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @return the provider config
   */
  @POST
  @RolesAllowed("users")
  @Path("/activities-focuses-joined-data")
  public Response getActivitiesFocusesJoinedData(@Context UriInfo uriInfo, @Context HttpServletRequest request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">> getActivitiesFocusesJoinedData");
    }

    ActivityManager activityManager = this.activityStatsService.getActivityManager();

    List<String> activityIds = new LinkedList<>();
    activityIds.add("1");

    List<ExoSocialActivity> activities = activityManager.getActivities(activityIds);

    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String currentUserName = convo.getIdentity().getUserId();

      ActivityFocusDAO activityFocusDAO = ExoContainerContext.getCurrentContainer()
                                                             .getComponentInstanceOfType(ActivityFocusDAO.class);

      List<ActivityFocusEntity> activityFocusRecords = null;
      if (activityFocusDAO != null) {
        activityFocusRecords = activityFocusDAO.findAllFocusOfUser(currentUserName, 0, "");
      }

      String jsonResponse = null;

      try {
        // activityFocusRecords.toArray()
        jsonResponse = Utils.asJSON(activities.toArray());

      } catch (Throwable e) {
        LOG.error("getActivitiesFocusesJoinedData error", e);
      }

      // LOG.info("RESTSmartActivityStreamStatisticService ActivityManager json(): ",
      // jsonResponse);

      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getActivitiesFocusesJoinedData");
      }
      return Response.status(Status.ACCEPTED).entity(jsonResponse).build();
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("<< getActivitiesFocusesJoinedData conversationState == null");
      }
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

}
