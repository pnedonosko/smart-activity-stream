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
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.SmartActivityService;
import org.exoplatform.smartactivitystream.stats.domain.ActivityFocusEntity;
import org.exoplatform.smartactivitystream.stats.domain.FocusId;

import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.filter.ActivityFilter;
import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.impl.ActivityBuilderWhere;

/**
 * The DAO layer for ActivityFocusEntity.
 */
public class ActivityFocusDAO extends GenericDAOJPAImpl<ActivityFocusEntity, FocusId> {

  /** The Constant LOG. */
  private static final Log LOG = ExoLogger.getLogger(SmartActivityService.class);

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
   * Find focus by user ID, activity ID and its start time.
   *
   * @param userId the user id
   * @param activityId the activity id
   * @param startTime the start time
   * @return the activity focus entity
   */
  public ActivityFocusEntity findFocus(String userId, String activityId, long startTime) {
    TypedQuery<ActivityFocusEntity> query = getEntityManager()
                                                              .createNamedQuery("SmartActivityFocus.findFocus",
                                                                                ActivityFocusEntity.class)
                                                              .setParameter("userId", userId)
                                                              .setParameter("activityId", activityId)
                                                              .setParameter("startTime", startTime);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
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
   * Find all focus records for given user.
   *
   * @param userId the user id
   * @return the list
   */
  public List<ActivityFocusEntity> findAllFocusOfUser(String userId, int timeScaleMinutes, String streamSelected) {

    TypedQuery<ActivityFocusEntity> query = getEntityManager()
                                                              .createNamedQuery("SmartActivityFocus.findAllFocusOfUser",
                                                                                ActivityFocusEntity.class)
                                                              .setParameter("userId", userId);

    try {
      return query.getResultList();
    } catch (NoResultException e) {
      return Collections.emptyList();
    }
  }

}
