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

//import static org.exoplatform.webconferencing.Utils.getResourceMessages;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.portlet.*;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.ContextInfo;
import org.exoplatform.smartactivitystream.SmartActivityService;
import org.exoplatform.smartactivitystream.Utils;
import org.exoplatform.smartactivitystream.stats.dao.ActivityFocusDAO;
import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusEntity;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.ws.frameworks.json.impl.JsonException;

import static org.exoplatform.smartactivitystream.Utils.getResourceMessages;

//import org.exoplatform.webconferencing.UserInfo;
//import org.exoplatform.webconferencing.WebConferencingService;

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

  /** The Web Conferencing service. */
  // private WebConferencingService webConferencing;

  /**
   * {@inheritDoc}
   */
  @Override
  public void init() throws PortletException {
    super.init();

    ExoContainer container = ExoContainerContext.getCurrentContainer();
    // this.webConferencing =
    // container.getComponentInstanceOfType(WebConferencingService.class);
  }

  /**
   * Admin view.
   *
   * @param request the request
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws PortletException the portlet exception
   */
  @Override
  public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    final String remoteUser = request.getRemoteUser();

    ActivityFocusDAO activityFocusDAO = ExoContainerContext.getCurrentContainer()
                                                           .getComponentInstanceOfType(ActivityFocusDAO.class);
    List<ActivityFocusEntity> activityFocusRecords = null;
    if (activityFocusDAO != null) {
      activityFocusRecords = activityFocusDAO.findAllFocusOfUser(remoteUser,60,"All streams");
    }

    String contextJson;
    try {
      contextJson = ContextInfo.getCurrentContext(request.getRemoteUser()).asJSON();
    } catch (JsonException e) {
      LOG.error("Error converting context to JSON", e);
      contextJson = null;
    }

    Map<String, String> messages = getResourceMessages("locale.smartactivity.SmartActivityStreamAdmin", request.getLocale());

    // Markup
    request.setAttribute("messages", messages);
    request.setAttribute("contextJson", contextJson);

    try {
      PortletRequestDispatcher prDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/pages/admin.jsp");

      prDispatcher.include(request, response);

    } catch (Exception e) {
      LOG.error("Error processing Smart Activity Stream Admin portlet for user " + remoteUser, e);
    }
  }

}
