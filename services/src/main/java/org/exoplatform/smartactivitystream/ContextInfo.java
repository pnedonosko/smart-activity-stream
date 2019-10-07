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
package org.exoplatform.smartactivitystream;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.smartactivitystream.cometd.CometdSmartActivityService;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: ContextInfo.java 00000 Oct 2, 2019 pnedonosko $
 */
public class ContextInfo {

  /**
   * Gets the current context.
   *
   * @param userId the user id
   * @return the current context
   */
  public static final ContextInfo getCurrentContext(String userId) {
    ExoContainer exo = ExoContainerContext.getCurrentContainer();
    CometdSmartActivityService cometdService = exo.getComponentInstanceOfType(CometdSmartActivityService.class);
    ContextInfo context;
    if (cometdService != null) {
      context = new ContextInfo(exo.getContext().getName(),
                                userId,
                                cometdService.getCometdServerPath(),
                                cometdService.getUserToken(userId));
    } else {
      context = new ContextInfo(exo.getContext().getName(), userId, null, null);
    }
    // if (locale != null) {
    // context.addMessages(getResourceMessages("locale.webconferencing.WebConferencingClient", locale));
    // }
    return context;
  }

  /** The eXo container name. */
  private final String containerName;

  /** CometD server URL path. */
  private final String cometdPath;

  /** CometD server token. */
  private final String cometdToken;

  /** eXo user name. */
  private final String userId;

  /**
   * Instantiates a new context info.
   *
   * @param containerName the container name
   * @param userId the user id
   * @param cometdPath the cometd path
   * @param cometdToken the cometd token
   */
  private ContextInfo(String containerName, String userId, String cometdPath, String cometdToken) {
    super();
    this.containerName = containerName;
    this.cometdPath = cometdPath;
    this.cometdToken = cometdToken;
    this.userId = userId;
  }

  /**
   * Gets the container name.
   *
   * @return the containerName
   */
  public String getContainerName() {
    return containerName;
  }

  /**
   * Gets the cometd path.
   *
   * @return the cometdPath
   */
  public String getCometdPath() {
    return cometdPath;
  }

  /**
   * Gets the cometd token.
   *
   * @return the cometdToken
   */
  public String getCometdToken() {
    return cometdToken;
  }

  /**
   * Gets the user id.
   *
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * As JSON.
   *
   * @return the string
   * @throws JsonException the json exception
   */
  public String asJSON() throws JsonException {
    JsonGeneratorImpl gen = new JsonGeneratorImpl();
    return gen.createJsonObject(this).toString();
  }

}
