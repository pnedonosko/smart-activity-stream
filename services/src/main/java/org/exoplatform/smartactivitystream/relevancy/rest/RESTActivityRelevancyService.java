/*
 * 
 */
package org.exoplatform.smartactivitystream.relevancy.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
 * The REST service for Activity Relevancy Service
 */
@Path("/smartactivity")
@Produces(MediaType.APPLICATION_JSON)
public class RESTActivityRelevancyService implements ResourceContainer {

  /** The Constant LOG. */
  protected static final Log               LOG = ExoLogger.getLogger(RESTActivityRelevancyService.class);

  /** The Activity Relevancy Service service */
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
   * @param relevanceEntity to be saved or updated
   * @return the response
   */
  @POST
  @RolesAllowed("users")
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/relevancy/{userId}/{activityId}")
  public Response saveRelevance(RelevanceEntity relevanceEntity) {

    if (isUserAllowed(relevanceEntity.getUserId())) {
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
        return Response.status(Status.NOT_FOUND).build();
      }
      return Response.ok().entity(relevanceEntity).build();
    }

    return Response.status(Status.FORBIDDEN)
                   .entity("{ \"error\" : \"Forbidden\", \"message\" : \"Cannot get relevancy for other users\" }")
                   .build();
  }

  @GET
  @RolesAllowed("users")
  @Path("/stats")
  @Produces("application/json")
  public Response getStats() {

    List<RelevanceStatsEntity> userStats = activityRelevancyService.findUserStats();
    if (userStats == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    long totalCount = activityRelevancyService.getRelevanciesCount();
    RelevanceStatsReport report = new RelevanceStatsReport(totalCount, userStats);
    
    try {
      String prettyJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(report);
      return Response.ok().entity(prettyJson).build();
    } catch (JsonProcessingException e) {
      LOG.warn("Error serializing stats report to pretty JSON: " + e.getMessage());
      return Response.ok().entity(report).build();
    }
  }

  /**
   * Checks if given userId is the current user
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
