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
  private static final Log LOG = ExoLogger.getLogger(ActivityStatsDAO.class);

  /**
   * Find stats records for given activity.
   *
   * @param activityId the activity id
   * @return the list
   */
  public ActivityStatsEntity findActivityStats(String activityId) {

    LOG.info("findActivityStats start");

    TypedQuery<ActivityStatsEntity> query = getEntityManager()
                                                              .createNamedQuery("SmartActivityStats.findActivityStats",
                                                                                ActivityStatsEntity.class)
                                                              .setParameter("activityId", activityId);

    LOG.info("findActivityStats start query");
    try {
      ActivityStatsEntity activityStatsEntity = query.getSingleResult();

      LOG.info("findActivityStats query finished successfully: " + activityStatsEntity);

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

    LOG.info("findActivityFocusChartData start");

    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("SmartActivityStats.findActivityFocusChartData", Tuple.class)
                                                .setParameter("activityId", activityId);

    LOG.info("findActivityFocusChartData start query");
    try {

      List<Tuple> activityFocusChartDataT = query.getResultList();

      List<String[]> activityFocusChartData = new LinkedList<>();
      for (Tuple tuple : activityFocusChartDataT) {
        activityFocusChartData.add(new String[] { Long.class.cast(tuple.get(0)).toString(),
            Long.class.cast(tuple.get(1)).toString() });
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

    LOG.info("findActivityFocuses start");

    TypedQuery<ActivityStatsEntity> query = getEntityManager()
                                                              .createNamedQuery("SmartActivityStats.findActivityFocuses",
                                                                                ActivityStatsEntity.class)
                                                              .setParameter("activityId", activityId)
                                                              .setParameter("scaleTime", Long.parseLong(scaleTime));

    LOG.info("findActivityFocuses start query");

    try {
      List<ActivityStatsEntity> activityStatsEntities = query.getResultList();

      LOG.info("findActivityFocuses query finished successfully: "
          + Arrays.toString(activityStatsEntities.toArray(new ActivityStatsEntity[0])));

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
    LOG.info("findMaxTotalShown start");

    TypedQuery<Long> queryTest = getEntityManager().createNamedQuery("SmartActivityStats.findMaxTotalFocus", Long.class);

    LOG.info("findMaxTotalShown start query");

    try {
      Long maxTotalShown = queryTest.getSingleResult();

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
