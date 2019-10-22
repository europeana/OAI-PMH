package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.service.exception.NoRecordsMatchException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
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
public class SearchApiTest extends SolrBasedProviderTestCase {
    private static final int IDENTIFIERS_PER_PAGE = 300;

    private static final String METADATA_FORMAT = "edm";

    private static final String DATE_1 = "2017-08-03T15:16:21Z";

    private static final String DATE_2 = "2017-08-03T12:16:21Z";

    private static final String DATE_3 = "2017-08-03T15:25:23Z";

    private static final String SET_1 = "2064125";

    private static final String SET_2 = "08506";

    private static final String LIST_IDENTIFIERS = "listIdentifiers";

    private static final String LIST_IDENTIFIERS_FROM = "listIdentifiersFrom";

    private static final String LIST_IDENTIFIERS_FROM_UNTIL = "listIdentifiersFromUntil";

    private static final String LIST_IDENTIFIERS_FROM_UNTIL_SET = "listIdentifiersFromUntilSet";

    private static final String LIST_IDENTIFIERS_SET = "listIdentifiersSet";

    private static final String LIST_IDENTIFIERS_UNTIL = "listIdentifiersUntil";

    @InjectMocks
    private SearchApi searchApi;

    @Test
    public void listIdentifiersGeneral() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, null, null, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, null, null, null);
    }

    @Test
    public void listIdentifiersSet() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_SET);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, null, null, SET_1, IDENTIFIERS_PER_PAGE);
        assertResults(result, null, null, SET_1);
    }

    @Test
    public void listIdentifiersFrom() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_FROM);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date from = DateConverter.fromIsoDateTime(DATE_1);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, from, null, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, from, null, null);
    }

    @Test
    public void listIdentifiersUntil() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_UNTIL);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date until = DateConverter.fromIsoDateTime(DATE_1);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, null, until, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, null, until, null);
    }

    @Test
    public void listIdentifiersFromUntil() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_FROM_UNTIL);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date from = DateConverter.fromIsoDateTime(DATE_2);
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, from, until, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, from, until, null);
    }

    @Test(expected = NoRecordsMatchException.class)
    public void listIdentifiersWithEmptyResult() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = Mockito.mock(QueryResponse.class);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Mockito.when(response.getResults()).thenReturn(new SolrDocumentList());
        Date from = DateConverter.fromIsoDateTime(DATE_1);
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        searchApi.listIdentifiers(METADATA_FORMAT, from, until, SET_2, IDENTIFIERS_PER_PAGE);
    }

    @Test
    public void listIdentifiersFromUntilSet() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_FROM_UNTIL_SET);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date from = DateConverter.fromIsoDateTime(DATE_1);
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, from, until, SET_2, IDENTIFIERS_PER_PAGE);
        assertResults(result, from, until, SET_2);
    }

    private void assertResults(ListIdentifiers results, Date from, Date until, String set) {
        assertNotNull(results);
        assertNotNull(results.getHeaders());
        assertFalse(results.getHeaders().isEmpty());
        for (Header header : results.getHeaders()) {
            assertNotNull(header.getIdentifier());
            Date timestamp = header.getDatestamp();
            assertNotNull(timestamp);
            if (from != null) {
                assertTrue(timestamp.equals(from) || timestamp.after(from));
            }
            if (until != null) {
                assertTrue(timestamp.before(until));
            }
            if (set != null) {
                assertEquals(1, header.getSetSpec().size());
                assertTrue(header.getSetSpec().get(0).equals(set));
            }
        }
        ResumptionToken token = results.getResumptionToken();
        if (token != null) {
            assertNotNull(token.getValue());
            assertTrue(token.getCursor() >= 0 && token.getCursor() < token.getCompleteListSize());
            assertNotNull(token.getExpirationDate());
        }
    }
}

