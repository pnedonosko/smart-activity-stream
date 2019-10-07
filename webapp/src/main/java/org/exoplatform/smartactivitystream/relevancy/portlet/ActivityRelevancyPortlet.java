package org.exoplatform.smartactivitystream.relevancy.portlet;

import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.ContextInfo;
import org.exoplatform.smartactivitystream.SmartActivityService;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.ws.frameworks.json.impl.JsonException;

public class ActivityRelevancyPortlet extends GenericPortlet {

  /** The Constant LOG. */
  private static final Log LOG = ExoLogger.getLogger(ActivityRelevancyPortlet.class);

  /**
   * Renders the portlet on the page.
   */
  @Override
  protected void doView(final RenderRequest request, final RenderResponse response) {
    SmartActivityService smartService = ExoContainerContext.getCurrentContainer()
                                                           .getComponentInstanceOfType(SmartActivityService.class);
    if (smartService != null && smartService.isTrackersEnabled()) {
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
