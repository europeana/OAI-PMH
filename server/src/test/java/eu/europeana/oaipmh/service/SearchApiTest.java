package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.service.exception.NoRecordsMatchException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
@PropertySources(value = {})
@SpringBootTest
public class SearchApiTest extends BaseApiTest {
    private static final String METADATA_FORMAT = "edm";

    private static final String DATE_1 = "2017-08-03T15:16:21Z";

    private static final String DATE_2 = "2017-08-03T12:16:21Z";

    private static final String DATE_3 = "2017-08-03T15:25:23Z";

    private static final String SET_1 = "2064125_Otto-Lilienthal-Museum";

    private static final String SET_2 = "08506_Ag_EU_ATHENA_Central_Library_of_the_Bulgaria";

    private static final String LIST_IDENTIFIERS = "listIdentifiers";

    private static final String LIST_IDENTIFIERS_FROM = "listIdentifiersFrom";

    private static final String LIST_IDENTIFIERS_FROM_UNTIL = "listIdentifiersFromUntil";

    private static final String LIST_IDENTIFIERS_FROM_UNTIL_SET = "listIdentifiersFromUntilSet";

    private static final String LIST_IDENTIFIERS_SET = "listIdentifiersSet";

    private static final String LIST_IDENTIFIERS_UNTIL = "listIdentifiersUntil";

    @Mock
    private CloudSolrClient solrClient;

    @InjectMocks
    private SearchApi searchApi;


    @Test
    public void listIdentifiersGeneral() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, null, null, null);
        assertResults(result, null, null, null);
    }

    private QueryResponse getResponse(String fileName) throws IOException {
        Path path = Paths.get(resDir + "/" + fileName);
        byte[] bytes = Files.readAllBytes(path);
        QueryResponse response = (QueryResponse) QueryResponse.deserialize(bytes);
        return response;
    }

    @Test
    public void listIdentifiersSet() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_SET);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, null, null, SET_1);
        assertResults(result, null, null, SET_1);
    }

    @Test
    public void listIdentifiersFrom() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_FROM);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date from = DateConverter.fromIsoDateTime(DATE_1);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, from, null, null);
        assertResults(result, from, null, null);
    }


    @Test
    public void listIdentifiersUntil() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_UNTIL);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date until = DateConverter.fromIsoDateTime(DATE_1);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, null, until, null);
        assertResults(result, null, until, null);
    }

    @Test
    public void listIdentifiersFromUntil() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_FROM_UNTIL);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date from = DateConverter.fromIsoDateTime(DATE_2);
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, from, until, null);
        assertResults(result, from, until, null);
    }

    @Test(expected = NoRecordsMatchException.class)
    public void listIdentifiersWithEmptyResult() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = Mockito.mock(QueryResponse.class);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Mockito.when(response.getResults()).thenReturn(new SolrDocumentList());
        Date from = DateConverter.fromIsoDateTime(DATE_1);
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        searchApi.listIdentifiers(METADATA_FORMAT, from, until, SET_2);
    }

    @Test
    public void listIdentifiersFromUntilSet() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_FROM_UNTIL_SET);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date from = DateConverter.fromIsoDateTime(DATE_1);
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        ListIdentifiers result = searchApi.listIdentifiers(METADATA_FORMAT, from, until, SET_2);
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
                assertNotNull(header.getSetSpec().contains(set));
            }
        }
        ResumptionToken token = results.getResumptionToken();
        if (token != null) {
            assertTrue(token.getValue() != null);
            assertTrue(token.getCursor() >= 0 && token.getCursor() < token.getCompleteListSize());
            assertTrue(token.getExpirationDate() != null);
        }
    }
}

