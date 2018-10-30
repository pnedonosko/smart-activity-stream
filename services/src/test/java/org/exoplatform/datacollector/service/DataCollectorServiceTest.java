package org.exoplatform.datacollector.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.datacollector.AbstractTest;
import org.exoplatform.datacollector.DataCollectorService;
import org.exoplatform.datacollector.TestUtils;
import org.exoplatform.datacollector.dao.RelevanceDAO;
import org.exoplatform.datacollector.domain.RelevanceEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataCollectorServiceTest extends AbstractTest {
  PortalContainer      container = null;

  DataCollectorService dataCollectorService;

  RelevanceDAO         relevanceStorage;

  @Before
  public void setUp() {
    container = PortalContainer.getInstance();

    /*
    RepositoryService jcrService = container.getComponentInstanceOfType(RepositoryService.class);
    SessionProviderService sessionProviders = container.getComponentInstanceOfType(SessionProviderService.class);
    NodeHierarchyCreator hierarchyCreator = container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    OrganizationService organization = container.getComponentInstanceOfType(OrganizationService.class);
    IdentityManager identityManager = container.getComponentInstanceOfType(IdentityManager.class);
    IdentityStorage identityStorage = container.getComponentInstanceOfType(IdentityStorage.class);
    ActivityManager activityManager = container.getComponentInstanceOfType(ActivityManager.class);
    
    dataCollectorService = new DataCollectorService(jcrService, sessionProviders, hierarchyCreator, organization, identityManager, identityStorage, activityManager, relevanceStorage);
    */
    relevanceStorage = mock(RelevanceDAO.class);

    dataCollectorService = new DataCollectorService(relevanceStorage);

    when(relevanceStorage.find(TestUtils.EXISTING_RELEVANCE_ID)).thenReturn(TestUtils.getExistingRelevance());
    when(relevanceStorage.find(TestUtils.UNEXISTING_RELEVANCE_ID)).thenReturn(null);
  }

  @Test
  public void testSaveRelevanceUpdate() {
    RelevanceEntity relevance = TestUtils.getExistingRelevance();
    dataCollectorService.saveRelevance(relevance);

    verify(relevanceStorage, times(1)).update(relevance);
  }

  @Test
  public void testSaveRelevance() {
    RelevanceEntity relevance = TestUtils.getNewRelevance();
    dataCollectorService.saveRelevance(relevance);

    verify(relevanceStorage, times(1)).create(relevance);
  }

  @Test
  public void testFindById() {
    dataCollectorService.findById(TestUtils.EXISTING_RELEVANCE_ID);

    verify(relevanceStorage, times(1)).find(TestUtils.EXISTING_RELEVANCE_ID);
  }

  @After
  public void tearDown() {
    dataCollectorService = null;
  }
}
