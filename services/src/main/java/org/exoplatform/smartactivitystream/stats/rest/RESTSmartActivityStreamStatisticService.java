/*
 * Copyright (C) 2003-2017 eXo Platform SAS.
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
package org.exoplatform.smartactivitystream.stats.rest;

import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
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
import org.exoplatform.smartactivitystream.SmartActivityService;
import org.exoplatform.smartactivitystream.Utils;
import org.exoplatform.smartactivitystream.stats.dao.ActivityFocusDAO;
import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusEntity;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: RESTSmartActivityStreamStatisticService.java 00000 Feb 22, 2017
 *          pnedonosko $ and Oct 22, 2019 Nikita Riabovol
 */
@Path("/smartactivity-stat")
@Produces(MediaType.APPLICATION_JSON)
public class RESTSmartActivityStreamStatisticService implements ResourceContainer {

  /** The Constant EMPTY. */
  public static final String           EMPTY = "".intern();

  /** The Constant LOG. */
  protected static final Log           LOG   = ExoLogger.getLogger(RESTSmartActivityStreamStatisticService.class);

  /** The web conferencing. */
  protected final SmartActivityService smartActivity;

  /** The cache control. */
  private final CacheControl           cacheControl;

  /**
   * Instantiates a new REST service for smart activity service.
   *
   * @param smartActivity
   */
  public RESTSmartActivityStreamStatisticService(SmartActivityService smartActivity) {
    this.smartActivity = smartActivity;
    this.cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }

  /**
   * Gets the provider config.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @return the provider config
   */
  @POST
  @RolesAllowed("users")
  @Path("/activities-joined-data")
  public Response getActivitiesJoinedData(@Context UriInfo uriInfo, @Context HttpServletRequest request) {
    LOG.info("RESTSmartActivityStreamStatisticService /smartactivity-stat/activities-joined-data");
    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String currentUserName = convo.getIdentity().getUserId();

      ActivityFocusDAO activityFocusDAO = ExoContainerContext.getCurrentContainer()
                                                             .getComponentInstanceOfType(ActivityFocusDAO.class);

      List<ActivityFocusEntity> activityFocusRecords = null;
      if (activityFocusDAO != null) {
        activityFocusRecords = activityFocusDAO.findAllFocusOfUser(currentUserName);
      }

      String jsonResponse = null;

      try {
        jsonResponse = Utils.asJSON(activityFocusRecords.toArray());

      } catch (Throwable e) {
        LOG.error("RESTSmartActivityStreamStatisticService activities-joined-data error", e);
      }

      LOG.info("RESTSmartActivityStreamStatisticService correct");
      return Response.status(Status.ACCEPTED).cacheControl(cacheControl).entity(jsonResponse).build();
    } else {
      LOG.info("RESTSmartActivityStreamStatisticService convo == null");
      return Response.status(Status.UNAUTHORIZED).cacheControl(cacheControl).build();
    }
  }
}
