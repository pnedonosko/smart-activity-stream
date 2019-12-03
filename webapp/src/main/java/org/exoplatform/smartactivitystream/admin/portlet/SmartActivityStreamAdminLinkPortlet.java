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

import static org.exoplatform.smartactivitystream.Utils.getResourceMessages;

import java.io.IOException;
import java.util.Map;

import javax.portlet.*;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:nik.riabovol@gmail.com">Nikita Riabovol</a>
 * @version $Id: SmartActivityStreamAdminLinkPortlet.java 00000 Dec 3, 2019
 *          NickEngineer $
 */
public class SmartActivityStreamAdminLinkPortlet extends GenericPortlet {

  /** The Constant LOG. */
  private static final Log    LOG                = ExoLogger.getLogger(SmartActivityStreamAdminLinkPortlet.class);

  private static final String STATS_FEATURE_NAME = "stats";

  /**
   * Admin menu link portlet.
   *
   * @param request the request
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws PortletException the portlet exception
   */
  @Override
  public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    final String currentUser = request.getRemoteUser();

    Map<String, String> messages = getResourceMessages("locale.smartactivity.SmartActivityStreamAdminLink", request.getLocale());

    // Markup
    request.setAttribute("messages", messages);

    try {
      PortletRequestDispatcher prDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/pages/admin-stats-link.jsp");

      if (CommonsUtils.isFeatureActive(STATS_FEATURE_NAME, currentUser)) {
        prDispatcher.include(request, response);
      }

    } catch (Exception e) {
      LOG.error("Error processing Smart Activity Stream Admin Link portlet for user " + currentUser, e);
    }
  }
}
