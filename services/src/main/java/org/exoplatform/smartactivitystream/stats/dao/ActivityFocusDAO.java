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
package org.exoplatform.smartactivitystream.stats.dao;

import java.util.*;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusEntity;
import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusId;

/**
 * The DAO layer for ActivityFocusEntity.
 */
public class ActivityFocusDAO extends GenericDAOJPAImpl<ActivityFocusEntity, ActivityFocusId> {

  /** The Constant LOG. */
  private static final Log LOG = ExoLogger.getLogger(ActivityFocusDAO.class);

  /**
   * Find total count.
   *
   * @return the long
   */
  public long findTotalCount() {
    try {
      TypedQuery<Long> query = getEntityManager().createNamedQuery("SmartActivityFocus.findTotalCount", Long.class);
      return query.getSingleResult().longValue();
    } catch (NoResultException e) {
      return -1;
    }
  }

  /**
   * Find all focus records for given user and activity.
   *
   * @param userId the user id
   * @param activityId the activity id
   * @return the list
   */
  public List<ActivityFocusEntity> findAllFocus(String userId, String activityId) {
    TypedQuery<ActivityFocusEntity> query = getEntityManager()
                                                              .createNamedQuery("SmartActivityFocus.findAllFocus",
                                                                                ActivityFocusEntity.class)
                                                              .setParameter("userId", userId)
                                                              .setParameter("activityId", activityId);
    try {
      return query.getResultList();
    } catch (NoResultException e) {
      return Collections.emptyList();
    }
  }

  /**
   * Find all focus records for given user and activity created after given start
   * date. Resulting list will be sorted in descending order of start time (last
   * record is first).
   *
   * @param userId the user id
   * @param activityId the activity id
   * @param startTimeAfter the start time after which search records
   * @return the list sorted by start time in descending order (last record is
   *         first)
   */
  public List<ActivityFocusEntity> findFocusAfter(String userId, String activityId, Long startTimeAfter) {
    TypedQuery<ActivityFocusEntity> query = getEntityManager()
                                                              .createNamedQuery("SmartActivityFocus.findFocusAfter",
                                                                                ActivityFocusEntity.class)
                                                              .setParameter("userId", userId)
                                                              .setParameter("activityId", activityId)
                                                              .setParameter("startTimeAfter", startTimeAfter);
    try {
      return query.getResultList();
    } catch (NoResultException e) {
      return Collections.emptyList();
    }
  }

  /**
   *
   * will change
   * Find all focus records for given activity.
   *
   * @param activityId the activity id
   * @return the list
   */
  public List<ActivityFocusEntity> findActivityFocus(String activityId) {

    String queryForActivityFocus =
                                 "SELECT NEW org.exoplatform.smartactivitystream.stats.domain.ActivityFocusEntity(MAX(f.userId), f.activityId, MIN(f.startTime), "
                                     + "MAX(f.stopTime), SUM(f.totalShown), SUM(f.contentShown), SUM(f.convoShown), SUM(f.contentHits), SUM(f.convoHits), "
                                     + "SUM(f.appHits), SUM(f.profileHits), SUM(f.linkHits), MAX(f.trackerVersion))  FROM SmartActivityFocus f "
                                     + "WHERE f.activityId = :activityId GROUP BY f.activityId";

    LOG.info("findActivityFocus start");

    TypedQuery<ActivityFocusEntity> query = getEntityManager().createQuery(queryForActivityFocus, ActivityFocusEntity.class)
                                                              .setParameter("activityId", activityId);
    ;

    LOG.info("findActivityFocus start query");
    try {

      List<ActivityFocusEntity> activityFocusEntities = query.getResultList();

      LOG.info("findActivityFocus query finished successfully: "
          + Arrays.toString(activityFocusEntities.toArray(new ActivityFocusEntity[0])));

      return activityFocusEntities;
    } catch (NoResultException e) {
      LOG.error("findActivityFocus NoResultException", e);
      return Collections.emptyList();
    } catch (Exception e) {
      LOG.error("findActivityFocus Exception", e);
      return Collections.emptyList();
    }
  }




}
