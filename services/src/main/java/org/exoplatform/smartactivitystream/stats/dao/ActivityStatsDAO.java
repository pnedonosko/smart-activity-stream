package org.exoplatform.smartactivitystream.stats.dao;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.stats.domain.ActivityStatsEntity;
import org.exoplatform.smartactivitystream.stats.domain.ActivityStatsId;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
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
  public ActivityStatsEntity findActivityStats(String activityId) {

    if (LOG.isDebugEnabled()) {
      LOG.debug(">>>> findActivityStats");
    }

    TypedQuery<ActivityStatsEntity> query = getEntityManager()
                                                              .createNamedQuery("SmartActivityStats.findActivityStats",
                                                                                ActivityStatsEntity.class)
                                                              .setParameter("activityId", activityId);

    if (LOG.isDebugEnabled()) {
      LOG.debug("start query");
    }

    try {
      ActivityStatsEntity activityStatsEntity = query.getSingleResult();

      if (LOG.isDebugEnabled()) {
        LOG.debug("query finished successfully: " + activityStatsEntity);
        LOG.debug("<<<< findActivityStats");
      }

      return activityStatsEntity;
    } catch (NoResultException e) {
      LOG.error("findActivityStats NoResultException", e);
      return null;
    } catch (Exception e) {
      LOG.error("findActivityStats Exception", e);
      return null;
    }
  }

  /**
   * Find activity focuses (startDate,focusNumber) for ChartData.
   *
   * @param activityId the activity id
   * @return the list
   */
  public List<String[]> findActivityFocusChartData(String activityId) {

    if (LOG.isDebugEnabled()) {
      LOG.debug(">>>> findActivityFocusChartData");
    }

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SmartActivityStats.findActivityFocusChartData", Tuple.class)
                                                .setParameter("activityId", activityId);

    if (LOG.isDebugEnabled()) {
      LOG.debug(">>>> start query");
    }

    try {

      List<Tuple> activityFocusChartDataT = query.getResultList();

      List<String[]> activityFocusChartData = new LinkedList<>();
      for (Tuple tuple : activityFocusChartDataT) {
        activityFocusChartData.add(new String[] { Long.class.cast(tuple.get(0)).toString(),
            Long.class.cast(tuple.get(1)).toString() });
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("query finished successfully: ");
        LOG.debug("<<<< findActivityFocusChartData");
      }

      return activityFocusChartData;
    } catch (NoResultException e) {
      LOG.error("findActivityFocusChartData NoResultException", e);
      return Collections.emptyList();
    } catch (Exception e) {
      LOG.error("findActivityFocusChartData Exception", e);
      return Collections.emptyList();
    }
  }

  /**
   * Find focus records for given activity.
   *
   * @param activityId the activity id
   * @return the list
   */
  public List<ActivityStatsEntity> findActivityFocuses(String activityId, String scaleTime) {

    if (LOG.isDebugEnabled()) {
      LOG.debug(">>>> findActivityFocuses");
    }

    TypedQuery<ActivityStatsEntity> query = getEntityManager()
                                                              .createNamedQuery("SmartActivityStats.findActivityFocuses",
                                                                                ActivityStatsEntity.class)
                                                              .setParameter("activityId", activityId)
                                                              .setParameter("scaleTime", Long.parseLong(scaleTime));

    if (LOG.isDebugEnabled()) {
      LOG.debug("start query");
    }

    try {
      List<ActivityStatsEntity> activityStatsEntities = query.setMaxResults(ACTIVITY_FOCUSES_MAX_RESULT).getResultList();

      if (LOG.isDebugEnabled()) {
        LOG.debug("query finished successfully: " + Arrays.toString(activityStatsEntities.toArray(new ActivityStatsEntity[0])));
        LOG.debug("<<<< findActivityFocuses");
      }

      return activityStatsEntities;
    } catch (NoResultException e) {
      LOG.error("findActivityFocuses NoResultException", e);
      return Collections.emptyList();
    } catch (Exception e) {
      LOG.error("findActivityFocuses Exception", e);
      return Collections.emptyList();
    }
  }

  /**
   * Find the max number of total shown.
   *
   * @return the max number of total shown
   */
  public Long findMaxTotalShown() {
    if (LOG.isDebugEnabled()) {
      LOG.debug(">>>> findMaxTotalShown");
    }

    TypedQuery<Long> queryTest = getEntityManager().createNamedQuery("SmartActivityStats.findMaxTotalFocus", Long.class);

    if (LOG.isDebugEnabled()) {
      LOG.debug("start query");
    }

    try {
      Long maxTotalShown = queryTest.getSingleResult();

      if (LOG.isDebugEnabled()) {
        LOG.debug("query finished successfully: ");
        LOG.debug("<<<< findMaxTotalShown");
      }

      return maxTotalShown;
    } catch (NoResultException e) {
      LOG.error("findMaxTotalShown NoResultException", e);
      return null;
    } catch (Exception e) {
      LOG.error("findMaxTotalShown Exception", e);
      return null;
    }
  }
}
