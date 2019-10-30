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
package org.exoplatform.smartactivitystream.relevancy;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.picocontainer.Startable;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.smartactivitystream.relevancy.dao.RelevanceDAO;
import org.exoplatform.smartactivitystream.relevancy.dao.RelevanceStatsDAO;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceEntity;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceId;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceStatsEntity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;

/**
 * The Class ActivityRelevancyService.
 */
public class ActivityRelevancyService implements Startable {

  /** The DAO for RelevanceEntity. */
  protected final RelevanceDAO      relevanceStorage;

  /** The relevance stats. */
  protected final RelevanceStatsDAO relevanceStats;

  /** The Constant LOG. */
  protected static final Log        LOG = ExoLogger.getLogger(ActivityRelevancyService.class);

  /**
   * Instantiates a ActivityRelevancyService.
   *
   * @param relevanceStorage is the DAO for RelevanceEntity
   */
  public ActivityRelevancyService(RelevanceDAO relevanceStorage) {
    this.relevanceStorage = relevanceStorage;
    this.relevanceStats = null; // TODO used by tests
  }

  /**
   * Instantiates a ActivityRelevancyService.
   *
   * @param jcrService the jcr service
   * @param sessionProviders the session providers
   * @param hierarchyCreator the hierarchy creator
   * @param organization the organization
   * @param identityManager the identity manager
   * @param identityStorage the identity storage
   * @param activityManager the activity manager
   * @param relevanceStorage the relevance storage
   * @param relevanceStats the relevance stats
   */
  public ActivityRelevancyService(RepositoryService jcrService,
                                  SessionProviderService sessionProviders,
                                  NodeHierarchyCreator hierarchyCreator,
                                  OrganizationService organization,
                                  IdentityManager identityManager,
                                  IdentityStorage identityStorage,
                                  ActivityManager activityManager,
                                  RelevanceDAO relevanceStorage,
                                  RelevanceStatsDAO relevanceStats) {

    this.relevanceStorage = relevanceStorage;
    this.relevanceStats = relevanceStats;

    //sessionProviders.getSessionProvider()
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
    // Nothing

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    // Nothing

  }

  /**
   * Saves a relevance to the storage. Updates the relevance if it already exists.
   * 
   * @param relevance to be saved/updated
   */
  public void saveRelevance(RelevanceEntity relevance) {
    RelevanceEntity existingRelevance = relevanceStorage.find(new RelevanceId(relevance.getUserId(), relevance.getActivityId()));
    relevance.setUpdateDate(new Date());

    if (existingRelevance == null) {
      relevanceStorage.create(relevance);
      if (LOG.isDebugEnabled()) {
        LOG.info("Relevance created: " + relevance);
      }
    } else {
      relevanceStorage.update(relevance);
      if (LOG.isDebugEnabled()) {
        LOG.info("Relevance updated: " + relevance);
      }
    }
  }

  /**
   * Deletes a relevance.
   *
   * @param relevance to be deleted
   */
  public void deleteRelevance(RelevanceEntity relevance) {
    relevanceStorage.delete(relevance);
    if (LOG.isDebugEnabled()) {
      LOG.info("Relevance deleted: " + relevance);
    }
  }

  /**
   * Gets RelevanceEntity by given RelevanceId which contains the user id and
   * activity id.
   *
   * @param relevanceId is the searching parameter for retrieving RelevanceEntity
   * @return found RelevanceEntity or null if there is no such RelevanceEntity.
   */
  public RelevanceEntity findById(RelevanceId relevanceId) {
    return relevanceStorage.find(relevanceId);
  }

  /**
   * Find user stats since given data to current time.
   *
   * @param sinceDate the given date since which collect the statistics
   * @return the list
   */
  public List<RelevanceStatsEntity> findUserStats(Date sinceDate) {
    return relevanceStats.findUsersStats(sinceDate);
  }

  /**
   * Gets the relevancies count.
   *
   * @return the relevancies count
   */
  public long getRelevanciesCount() {
    return relevanceStorage.findTotalCount();
  }

}
