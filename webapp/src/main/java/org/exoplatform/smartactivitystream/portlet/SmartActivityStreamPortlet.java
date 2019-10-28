/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.smartactivitystream.portlet;

import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.stats.ActivityStatsService;
import org.exoplatform.smartactivitystream.stats.ContextInfo;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.ws.frameworks.json.impl.JsonException;

/**
 * The Class SmartActivityStreamPortlet.
 */
public class SmartActivityStreamPortlet extends GenericPortlet {

  /** The Constant LOG. */
  private static final Log LOG = ExoLogger.getLogger(SmartActivityStreamPortlet.class);

  /**
   * Renders the portlet on the page.
   */
  @Override
  protected void doView(final RenderRequest request, final RenderResponse response) {
    ActivityStatsService statsService = ExoContainerContext.getCurrentContainer()
                                                           .getComponentInstanceOfType(ActivityStatsService.class);
    if (statsService != null && statsService.isTrackersEnabled()) {
      String contextJson;
      try {
        contextJson = ContextInfo.getCurrentContext(request.getRemoteUser()).asJSON();
      } catch (JsonException e) {
        LOG.error("Error converting context to JSON", e);
        contextJson = null;
      }
      
      JavascriptManager js = ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).getJavascriptManager();
      js.require("SHARED/smartactivity", "smartactivity").addScripts("smartactivity.init(" + contextJson + ");");
    }
  }
}
