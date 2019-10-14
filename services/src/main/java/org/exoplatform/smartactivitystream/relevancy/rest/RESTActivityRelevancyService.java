/*
 * 
 */
package org.exoplatform.smartactivitystream.relevancy.rest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.smartactivitystream.relevancy.ActivityRelevancyService;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceEntity;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceId;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceStatsEntity;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceStatsReport;

/**
 * The REST service for Activity Relevancy Service.
 */
@Path("/smartactivity")
@Produces(MediaType.APPLICATION_JSON)
public class RESTActivityRelevancyService implements ResourceContainer {

  /** The Constant LOG. */
  protected static final Log               LOG = ExoLogger.getLogger(RESTActivityRelevancyService.class);

  /**  The Activity Relevancy Service service. */
  protected final ActivityRelevancyService activityRelevancyService;

  /**
   * Instantiates a new RESTActivityRelevancyService.
   *
   * @param activityRelevancyService the activity relevancy service
   */
  public RESTActivityRelevancyService(ActivityRelevancyService activityRelevancyService) {
    this.activityRelevancyService = activityRelevancyService;
  }

  /**
   * Posts a relevance.
   *
   * @param userId the user id
   * @param activityId the activity id
   * @param relevanceEntity to be saved or updated
   * @return the response
   */
  @POST
  @RolesAllowed("users")
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/relevancy/{userId}/{activityId}")
  public Response saveRelevance(@PathParam("userId") String userId,
                                @PathParam("activityId") String activityId,
                                RelevanceEntity relevanceEntity) {

    if (isUserAllowed(relevanceEntity.getUserId())) {

      if (!relevanceEntity.getUserId().equals(userId)) {
        return Response.status(Status.BAD_REQUEST)
                       .entity("{ \"error\" : \"Bad request\", \"message\" : \"User ID doesn't match\" }")
                       .build();
      }

      if (!relevanceEntity.getActivityId().equals(activityId)) {
        return Response.status(Status.BAD_REQUEST)
                       .entity("{ \"error\" : \"Bad request\", \"message\" : \"Activity ID doesn't match\" }")
                       .build();
      }

      activityRelevancyService.saveRelevance(relevanceEntity);
      return Response.ok().build();
    }

    return Response.status(Status.FORBIDDEN)
                   .entity("{ \"error\" : \"Forbidden\", \"message\" : \"Cannot set relevancy for other users\" }")
                   .build();
  }

  /**
   * Gets a relevance.
   *
   * @param userId the user id
   * @param activityId the activity id
   * @return response 200 which contains relevanceEntity or 404
   */
  @GET
  @RolesAllowed("users")
  @Path("/relevancy/{userId}/{activityId}")
  @Produces("application/json")
  public Response getRelevance(@PathParam("userId") String userId, @PathParam("activityId") String activityId) {

    if (isUserAllowed(userId)) {
      RelevanceEntity relevanceEntity = activityRelevancyService.findById(new RelevanceId(userId, activityId));
      if (relevanceEntity == null) {
        return Response.status(Status.NOT_FOUND)
                       .entity("{ \"error\" : \"Not found\", \"message\" : \"Relevancy not found\" }")
                       .build();
      }

      try {
        String prettyJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(relevanceEntity);
        return Response.ok().entity(prettyJson).build();
      } catch (JsonProcessingException e) {
        LOG.warn("Error serializing relevancy to pretty JSON: " + e.getMessage());
        return Response.ok().entity(relevanceEntity).build();
      }

    }

    return Response.status(Status.FORBIDDEN)
                   .entity("{ \"error\" : \"Forbidden\", \"message\" : \"Cannot get relevancy for other users\" }")
                   .build();
  }

  /**
   * Gets the stats.
   *
   * @param sinceDaysStr the since days str
   * @return the stats
   */
  @GET
  @RolesAllowed("users")
  @Path("/stats")
  @Produces("application/json")
  public Response getStats(@QueryParam("since_days") String sinceDaysStr) {
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      int sinceDays;
      if (sinceDaysStr != null && sinceDaysStr.length() > 0) {
        try {
          sinceDays = Integer.parseInt(sinceDaysStr);
          if (sinceDays <= 0) {
            LOG.warn("Wrong paramater since_days '" + sinceDaysStr + "': should be greater of zero");
            return Response.status(Status.BAD_REQUEST)
                           .entity("{ \"error\" : \"Client error\", \"message\" : \"Parameter 'since_days' should be greater of zero\" }")
                           .build();
          }
        } catch (NumberFormatException e) {
          LOG.warn("Wrong parameter format since_days '" + sinceDaysStr + "': " + e.getMessage());
          return Response.status(Status.BAD_REQUEST)
                         .entity("{ \"error\" : \"Client error\", \"message\" : \"Parameter 'since_days' has wrong format\" }")
                         .build();
        }
      } else {
        sinceDays = 90;
      }
      Calendar since = Calendar.getInstance();
      since.add(Calendar.DAY_OF_MONTH, -sinceDays);
      since.set(Calendar.HOUR_OF_DAY, 0);
      since.set(Calendar.MINUTE, 0);
      since.set(Calendar.SECOND, 0);
      since.set(Calendar.MILLISECOND, 1);
      List<RelevanceStatsEntity> userStats = activityRelevancyService.findUserStats(since.getTime());
      if (userStats == null) {
        return Response.status(Status.NOT_FOUND)
                       .entity("{ \"error\" : \"Not found\", \"message\" : \"User stats not found\" }")
                       .build();
      }
      long totalCount = activityRelevancyService.getRelevanciesCount();
      RelevanceStatsReport report = new RelevanceStatsReport(totalCount, userStats);
      report.setDescription("Statistics since " + dateFormat.format(since.getTime()));
      try {
        String prettyJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(report);
        return Response.ok().entity(prettyJson).build();
      } catch (JsonProcessingException e) {
        LOG.warn("Error serializing stats report to pretty JSON: " + e.getMessage());
        return Response.ok().entity(report).build();
      }
    } catch (Throwable e) {
      LOG.error("Error getting activity relevancy statistics: ", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{ \"error\" : \"Internal error\", \"message\" : \"" + e.getMessage() + "\" }")
                     .build();
    }
  }

  /**
   * Checks if given userId is the current user.
   *
   * @param userId to be checked
   * @return true if userId is equal to the current userId
   */
  private boolean isUserAllowed(String userId) {
    ConversationState conversationState = ConversationState.getCurrent();

    if (conversationState != null) {
      String currentUserId = conversationState.getIdentity().getUserId();
      return currentUserId.equals(userId);
    }

    return false;
  }
}
