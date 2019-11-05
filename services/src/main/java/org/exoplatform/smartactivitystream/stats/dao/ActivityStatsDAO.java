package org.exoplatform.smartactivitystream.stats.dao;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.smartactivitystream.stats.domain.ActivityStatsEntity;
import org.exoplatform.smartactivitystream.stats.domain.ActivityStatsId;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public List<ActivityStatsEntity> findActivityStats(String activityId) {

        LOG.info("findActivityStats start");

        TypedQuery<ActivityStatsEntity> query = getEntityManager().createNamedQuery("SmartActivityStats.findActivityStats", ActivityStatsEntity.class)
                .setParameter("activityId", activityId);


        LOG.info("findActivityStats start query");
        try {

            List<ActivityStatsEntity> activityStatsEntities = query.getResultList();

            LOG.info("findActivityStats query finished successfully: "
                    + Arrays.toString(activityStatsEntities.toArray(new ActivityStatsEntity[0])));

            return activityStatsEntities;
        } catch (NoResultException e) {
            LOG.error("findActivityStats NoResultException", e);
            return Collections.emptyList();
        } catch (Exception e) {
            LOG.error("findActivityStats Exception", e);
            return Collections.emptyList();
        }
    }
}
