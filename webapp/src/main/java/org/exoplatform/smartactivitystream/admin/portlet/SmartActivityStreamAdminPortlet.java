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
package org.exoplatform.smartactivitystream.admin.portlet;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.portlet.*;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.admin.model.CustomUserConnection;
import org.exoplatform.smartactivitystream.stats.ActivityStatsService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;

import static org.exoplatform.smartactivitystream.Utils.getResourceMessages;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:nik.riabovol@gmail.com">Nikita Riabovol</a>
 * @version $Id: SmartActivityStreamAdminPortlet.java 00000 Oct 15, 2019
 *          NickEngineer $
 */
public class SmartActivityStreamAdminPortlet extends GenericPortlet {

  /** The Constant LOG. */
  private static final Log LOG = ExoLogger.getLogger(SmartActivityStreamAdminPortlet.class);

  /**
   * Admin view.
   *
   * @param request the request
   * @param response the response (the statistics page and attributes (local
   *          messages, user spaces, user connections))
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws PortletException the portlet exception
   */
  @Override
  public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    final String currentUserId = request.getRemoteUser();

    ActivityStatsService activityStatsService = ExoContainerContext.getCurrentContainer()
                                                                   .getComponentInstanceOfType(ActivityStatsService.class);

    List<Space> userSpaces = activityStatsService.getUserSpaces(currentUserId);

    List<Identity> userConnections = activityStatsService.getUserConnections(currentUserId);
    List<CustomUserConnection> customUserConnections = new LinkedList<>();

    for (Identity connectionIdentity : userConnections) {
      customUserConnections.add(new CustomUserConnection(connectionIdentity.getProfile().getId(),
                                                         connectionIdentity.getProfile().getFullName()));
    }

    Map<String, String> messages = getResourceMessages("locale.smartactivity.SmartActivityStreamAdmin", request.getLocale());

    // Markup
    // Messages in the user locale
    request.setAttribute("messages", messages);
    // Available current user spaces
    request.setAttribute("userSpaces", userSpaces.toArray());
    // Available current user connections
    request.setAttribute("userConnections", customUserConnections.toArray());

    try {
      PortletRequestDispatcher prDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/pages/admin.jsp");

      prDispatcher.include(request, response);

    } catch (Exception e) {
      LOG.error("Error processing Smart Activity Stream Admin portlet for user " + currentUserId, e);
    }
  }
}
