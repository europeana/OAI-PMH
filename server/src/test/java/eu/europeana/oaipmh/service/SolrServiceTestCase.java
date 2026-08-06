package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.AbstractIntegrationIT;
import eu.europeana.oaipmh.config.OaiPmhSettings;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.ResumptionToken;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test case provides a base implementation for testing Solr-based OAI-PMH providers.
 * It defines common functionality and test utilities for validating the behavior of listSets
 * and listIdentifiers operations. The class is intended to be extended by specific test cases
 * for implementations of Solr-based providers.
 *
 * This test case utilizes mock objects for dependencies such as {@link CloudSolrClient} and
 * {@link OaiPmhSettings}, and provides methods for asserting correctness of the results
 * returned by list operations.
 *
 * NOTE: a real-time local solr is created for the IT, but there is no schema or configuration added
 *        to insert any data. Hence, for tests that are dependent on the data in solr,
 *        we have mocked a CloudSolrClient in this class
 */
public class SolrServiceTestCase extends AbstractIntegrationIT  {

    protected static final String METADATA_FORMAT  = "edm";
    protected static final String DATE_1           = "2017-08-02T22:00:00Z";
    protected static final String DATE_2           = "2017-08-03T12:16:21Z";
    protected static final String DATE_3           = "2017-08-03T15:25:23Z";
    protected static final String SET_1            = "2064125";
    protected static final String SET_2            = "08506";
    protected static final String LIST_SETS        = "listSets";
    protected static final String LIST_SETS_FROM   = "listSetsFrom";
    protected static final long COMPLETE_LIST_SIZE = 500;
    protected static final String EARLIEST_TIMESTAMP= "earliestTimestampResponse";

    protected static final String LIST_IDENTIFIERS      = "listIdentifiers";
    protected static final String LIST_IDENTIFIERS_SET  = "listIdentifiersSet";
    protected static final String LIST_IDENTIFIERS_UNTIL= "listIdentifiersUntil";
    protected static final String LIST_IDENTIFIERS_FROM = "listIdentifiersFrom";
    protected static final String LIST_IDENTIFIERS_FROM_UNTIL = "listIdentifiersFromUntil";
    protected static final String LIST_IDENTIFIERS_FROM_UNTIL_SET = "listIdentifiersFromUntilSet";
    protected static final String LIST_SETS_WITH_RESUMPTION_TOKEN_SECOND_PAGE = "listSetsWithResumptionTokenSecondPage";


    @Mock
    protected CloudSolrClient solrClient;

    @Mock
    OaiPmhSettings settings;

    @BeforeEach
    public void set() {
        Mockito.when(settings.getSetsPerPage()).thenReturn(2000);
        Mockito.when(settings.getRepositoryName()).thenReturn(REPOSITORY_NAME);
        Mockito.when(settings.getBaseUrl()).thenReturn(BASE_URL);
        Mockito.when(settings.getProtocolVersion()).thenReturn(PROTOCOL_VERSION);
        Mockito.when(settings.getDeletedRecord()).thenReturn(DELETED_RECORD);
        Mockito.when(settings.getGranularity()).thenReturn(GRANULARITY);
    }

    QueryResponse getResponse(String fileName) throws IOException {
        try (InputStream is = SolrServiceTestCase.class.getClassLoader().getResourceAsStream(fileName)) {
            NamedList<Object> result = processResponse(is, null);
            QueryResponse response = new QueryResponse();
            response.setResponse(result);
            return response;
        }
    }

    private NamedList<Object> processResponse(InputStream body, Object o) {
        XMLResponseParser parser= new XMLResponseParser();
        return parser.processResponse(body, "UTF-8");
    }


    protected void assertResults(ListSets results) {
        assertNotNull(results);
        assertNotNull(results.stream());
        assertFalse(results.isEmpty());
        results.stream().forEach(set -> {
            assertNotNull(set.getSetSpec());
            assertNotNull(set.getSetName());
        });
        ResumptionToken token = results.getResumptionToken();
        if (token != null) {
            assertNotNull(token.getValue());
            assertTrue(token.getCursor() >= 0 && token.getCursor() < token.getCompleteListSize());
            assertNotNull(token.getExpirationDate());
        }
    }

    protected void assertResults(ListIdentifiers results, Date from, Date until, String set) {
        Assertions.assertNotNull(results);
        Assertions.assertNotNull(results.stream());
        assertFalse(results.isEmpty());
        results.stream().forEach(header -> {
            Assertions.assertNotNull(header.getIdentifier());
            Date timestamp = header.getDatestamp();
            Assertions.assertNotNull(timestamp);
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
        });
        ResumptionToken token = results.getResumptionToken();
        if (token != null) {
            Assertions.assertNotNull(token.getValue());
            assertTrue(token.getCursor() >= 0 && token.getCursor() < token.getCompleteListSize());
            Assertions.assertNotNull(token.getExpirationDate());
        }
    }

}
