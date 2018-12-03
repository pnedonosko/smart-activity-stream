/*
 * 
 */
package org.exoplatform.smartactivitystream.relevancy.dao;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceEntity;
import org.exoplatform.smartactivitystream.relevancy.domain.RelevanceId;

/**
 * The DAO layer for RelevanceEntity.
 */
public class RelevanceDAO extends GenericDAOJPAImpl<RelevanceEntity, RelevanceId> {
  
  public long findTotalCount() {
    try {
      TypedQuery<Long> query = getEntityManager().createNamedQuery("SmartActivityRelevance.findCount", Long.class);
      return query.getSingleResult().longValue();
    } catch (NoResultException e) {
      return -1;
    }
  }

}
