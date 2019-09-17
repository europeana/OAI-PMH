package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.Set;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.ResumptionTokenHelper;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySources;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
@PropertySources(value = {})
@SpringBootTest
public class DefaultSetsProviderTest extends SolrBasedProviderTestCase {
    private static final String LIST_SETS = "listSets";

    private static final String LIST_SETS_WITH_RESUMPTION_TOKEN_SECOND_PAGE = "listSetsWithResumptionTokenSecondPage";

    private static final long RESUMPTION_TOKEN_TTL = 86400000;

    private static final long COMPLETE_LIST_SIZE = 500;

    @InjectMocks
    private DefaultSetsProvider setsProvider;

    @Test
    public void listSets() throws IOException, SolrServerException, OaiPmhException {
        QueryResponse response = getResponse(LIST_SETS);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        ListSets result = setsProvider.listSets();
        assertResults(result);
    }

    private void assertResults(ListSets results) {
        assertNotNull(results);
        assertNotNull(results.getSets());
        assertFalse(results.getSets().isEmpty());
        for (Set set : results.getSets()) {
            assertNotNull(set.getSetSpec());
            assertNotNull(set.getSetName());
        }
        ResumptionToken token = results.getResumptionToken();
        if (token != null) {
            assertNotNull(token.getValue());
            assertTrue(token.getCursor() >= 0 && token.getCursor() < token.getCompleteListSize());
            assertNotNull(token.getExpirationDate());
        }
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