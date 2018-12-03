/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
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
package org.exoplatform.smartactivitystream.relevancy.domain;

import java.util.List;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: RelevanceStatsReport.java 00000 Nov 19, 2018 pnedonosko $
 */
public class RelevanceStatsReport {

  /** The relevancies count. */
  protected Long                       relevanciesCount;

  /** The description. */
  protected String                     description;

  /** The user stats. */
  protected List<RelevanceStatsEntity> userStats;

  /**
   * Instantiates a new relevance stats repost.
   *
   * @param relevanciesCount the relevancies count
   * @param userStats the user stats
   */
  public RelevanceStatsReport(Long relevanciesCount, List<RelevanceStatsEntity> userStats) {
    super();
    this.relevanciesCount = relevanciesCount;
    this.userStats = userStats;
  }

  /**
   * Gets all relevancies count.
   *
   * @return the relevancies count
   */
  public long getAllRelevanciesCount() {
    return relevanciesCount;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets participated user stats.
   *
   * @return the user stats
   */
  public List<RelevanceStatsEntity> getUserStats() {
    return userStats;
  }

}
