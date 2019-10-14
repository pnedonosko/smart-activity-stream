/*
 * 
 */
package org.exoplatform.smartactivitystream.relevancy.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceId;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceStatsEntity;

/**
 * The DAO layer for RelevanceEntity.
 */
public class RelevanceStatsDAO extends GenericDAOJPAImpl<RelevanceStatsEntity, RelevanceId> {

  /**
   * Find users stats.
   *
   * @param afterDate the after date
   * @return the list
   */
  public List<RelevanceStatsEntity> findUsersStats(Date afterDate) {
    try {
      TypedQuery<RelevanceStatsEntity> query = getEntityManager()
                                                                 .createNamedQuery("SmartActivityRelevancyStats.findStats",
                                                                                   RelevanceStatsEntity.class)
                                                                 .setParameter("afterDate", afterDate);
      return query.getResultList();
    } catch (NoResultException e) {
      return Collections.emptyList();
    }
  }

}
