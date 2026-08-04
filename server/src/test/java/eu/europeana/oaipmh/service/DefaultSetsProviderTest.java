package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import eu.europeana.oaipmh.util.ResumptionTokenHelper;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.IOException;
import java.util.Date;

/**
 * Test class for validating the functionality of the DefaultSetsProvider implementation.
 * Extends the SolrBasedProviderTestCase to utilize common test utilities and mock setup
 * for Solr-based OAI-PMH providers.
 *
 * This class contains unit tests to validate the behavior of the `listSets` method under
 * different conditions, including cases with date filters and resumption tokens. The tests
 * rely on the use of mocked CloudSolrClient and utility methods for generating test responses.
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class DefaultSetsProviderTest extends SolrServiceTestCase {


    @Test
    public void listSets() throws IOException, SolrServerException, OaiPmhException {
        QueryResponse response = getResponse(LIST_SETS);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        ListSets result =  setsProvider.listSets(null, null);
        assertResults(result);
    }

    @Test
    public void listSetsFrom() throws IOException, SolrServerException, OaiPmhException {
        QueryResponse response = getResponse(LIST_SETS_FROM);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        Date from = DateConverter.fromIsoDateTime(DATE_1);
        ListSets result = setsProvider.listSets(from, null);
        assertResults(result);
    }

    @Test
    public void listSetsUntil() throws IOException, SolrServerException, OaiPmhException {
        QueryResponse response = getResponse(LIST_SETS_FROM);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        Date until = DateConverter.fromIsoDateTime(DATE_2);
        ListSets result = setsProvider.listSets(null, until);
        assertResults(result);
    }

    @Test
    public void listSetsWithResumptionToken() throws IOException, SolrServerException, OaiPmhException {
        QueryResponse response = getResponse(LIST_SETS_WITH_RESUMPTION_TOKEN_SECOND_PAGE);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        ResumptionToken token = ResumptionTokenHelper.createResumptionToken(new Date(System.currentTimeMillis() + RESUMPTION_TOKEN_TTL), COMPLETE_LIST_SIZE, 0);
        ListSets result = setsProvider.listSets(token);
        assertResults(result);
    }
}