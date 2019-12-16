package org.exoplatform.smartactivitystream.stats.dao;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.stats.domain.ActivityStatsEntity;
import org.exoplatform.smartactivitystream.stats.domain.ActivityStatsId;
import org.exoplatform.smartactivitystream.stats.domain.ChartPoint;

import javax.persistence.*;
import java.util.*;

/**
 * The DAO layer for ActivityStatsEntity.
 */
public class ActivityStatsDAO extends GenericDAOJPAImpl<ActivityStatsEntity, ActivityStatsId> {

  /** The Constant LOG. */
  private static final Log LOG                         = ExoLogger.getLogger(ActivityStatsDAO.class);

  /** The max result set for the activity focuses request. */
  public static final int  ACTIVITY_FOCUSES_MAX_RESULT = 1000;

  /**
   * Find stats records for given activity.
   *
   * @param activityId the activity id
   * @return the list
   */
  public ActivityStatsEntity findActivityStats(String activityId) throws Exception {

    if (LOG.isDebugEnabled()) {
      LOG.debug(">>>> findActivityStats");
    }

    TypedQuery<ActivityStatsEntity> query = getEntityManager()
                                                              .createNamedQuery("SmartActivityStats.findActivityStats",
                                                                                ActivityStatsEntity.class)
                                                              .setParameter("activityId", activityId);

    try {
      ActivityStatsEntity activityStatsEntity = query.getSingleResult();

      if (LOG.isDebugEnabled()) {
        LOG.debug("<<<< findActivityStats query finished successfully: " + activityStatsEntity);
      }

      return activityStatsEntity;
    } catch (NoResultException e) {
      LOG.warn("Cannot find such activity {" + activityId + "}", e);
      // skip activity (don't return in the result list)
      return null;
    } catch (NonUniqueResultException e) {
      LOG.warn("More than one result for such activity {" + activityId + "}", e);
      // skip activity (don't return in the result list)
      return null;
    } catch (Exception e) {
      LOG.error("Error reading statistics for activity {" + activityId + "}", e);
      throw new Exception("Sorry, error reading statistics for activity {" + activityId + "}");
    }
  }

  /**
   * Find activity focuses (startDate,focusNumber) for ChartData.
   *
   * @param activityId the activity id
   * @return the list
   */
  public List<ChartPoint> findActivityFocusChartData(String activityId) throws Exception {

    if (LOG.isDebugEnabled()) {
      LOG.debug(">>>> findActivityFocusChartData");
    }

    TypedQuery<ChartPoint> query = getEntityManager().createNamedQuery("SmartActivityStats.findActivityFocusChartData",
                                                                       ChartPoint.class)
                                                     .setParameter("activityId", activityId);

    try {
      List<ChartPoint> activityFocusChartData = query.getResultList();

      if (LOG.isDebugEnabled()) {
        LOG.debug("<<<< findActivityFocusChartData query finished successfully");
      }

      return activityFocusChartData;
    } catch (NoResultException e) {
      LOG.warn("Cannot find activityFocusChartData for activity {" + activityId + "}", e);
      return Collections.emptyList();
    } catch (Exception e) {
      LOG.error("Error reading activityFocusChartData for activity {" + activityId + "}", e);
      throw new Exception("Sorry, error reading activityFocusChartData for activity {" + activityId + "}");
    }
  }

  /**
   * Find focus records for given activity.
   *
   * @param activityId the activity id
   * @return the list
   */
  public List<ActivityStatsEntity> findActivityFocuses(String activityId, String scaleTime) throws Exception {

    if (LOG.isDebugEnabled()) {
      LOG.debug(">>>> findActivityFocuses");
    }

    TypedQuery<ActivityStatsEntity> query = getEntityManager()
                                                              .createNamedQuery("SmartActivityStats.findActivityFocuses",
                                                                                ActivityStatsEntity.class)
                                                              .setParameter("activityId", activityId)
                                                              .setParameter("scaleTime", Long.parseLong(scaleTime));

    try {
      List<ActivityStatsEntity> activityStatsEntities = query.setMaxResults(ACTIVITY_FOCUSES_MAX_RESULT).getResultList();

      if (LOG.isDebugEnabled()) {
        LOG.debug("<<<< findActivityFocuses query finished successfully");
      }

      return activityStatsEntities;
    } catch (NoResultException e) {
      LOG.error("Error finding activityStatsEntities for activity {" + activityId + "}", e);
      throw new Exception("Sorry, error finding activityStatsEntities for activity {" + activityId + "}");
    } catch (Exception e) {
      LOG.error("Error reading activityStatsEntities for activity {" + activityId + "}", e);
      throw new Exception("Sorry, error reading activityStatsEntities for activity {" + activityId + "}");
    }
  }

  /**
   * Find the max number of total shown.
   *
   * @return the max number of total shown
   */
  public Long findMaxTotalShown() throws Exception {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">>>> findMaxTotalShown");
    }

    TypedQuery<Long> queryTest = getEntityManager().createNamedQuery("SmartActivityStats.findMaxTotalFocus", Long.class);

    try {
      Long maxTotalShown = queryTest.getSingleResult();

      if (LOG.isDebugEnabled()) {
        LOG.debug("<<<< findMaxTotalShown query finished successfully");
      }

      return maxTotalShown;
    } catch (NoResultException e) {
      LOG.warn("Cannot find maxTotalShown value", e);
      // statistics is empty
      return null;
    } catch (NonUniqueResultException e) {
      LOG.warn("More than one result for maxTotalShown value", e);
      throw new Exception("Sorry, error: more than one result for maxTotalShown value");
    } catch (Exception e) {
      LOG.error("Error reading maxTotalShown value", e);
      throw new Exception("Sorry, error reading maxTotalShown value");
    }
  }
}
