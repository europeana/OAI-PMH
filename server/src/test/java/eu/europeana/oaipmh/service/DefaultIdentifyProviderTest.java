package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
@PropertySources(value = {})
@SpringBootTest
public class DefaultIdentifyProviderTest extends SolrBasedProviderTest {
    private static final String REPOSITORY_NAME="Europeana OAI Endpoint v2.0";

    private static final String BASE_URL="https://oai.europeana.eu/oai";

    private static final String PROTOCOL_VERSION="2.0";

    private static final String EARLIEST_DATESTAMP="1970-01-01T00:00:00Z";

    private static final String DELETED_RECORD="no";

    private static final String GRANULARITY="YYYY-MM-DDThh:mm:ssZ";

    private static final String ADMIN_EMAIL="api@europeana.eu";

    private static final String COMPRESSION="gzip";

    private static final String IDENTIFY = "identify";

    @InjectMocks
    private DefaultIdentifyProvider defaultIdentifyProvider;

    @Before
    public void initTest() {
        ReflectionTestUtils.setField(defaultIdentifyProvider, "repositoryName", REPOSITORY_NAME);
        ReflectionTestUtils.setField(defaultIdentifyProvider, "baseURL", BASE_URL);
        ReflectionTestUtils.setField(defaultIdentifyProvider, "protocolVersion", PROTOCOL_VERSION);
        ReflectionTestUtils.setField(defaultIdentifyProvider, "earliestDatestamp", EARLIEST_DATESTAMP);
        ReflectionTestUtils.setField(defaultIdentifyProvider, "deletedRecord", DELETED_RECORD);
        ReflectionTestUtils.setField(defaultIdentifyProvider, "granularity", GRANULARITY);
        ReflectionTestUtils.setField(defaultIdentifyProvider, "adminEmail", new String[] {ADMIN_EMAIL}, String[].class);
        ReflectionTestUtils.setField(defaultIdentifyProvider, "compression", new String[] {COMPRESSION}, String[].class);
    }

    @Test
    public void provideIdentify() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(IDENTIFY);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        Identify identify = defaultIdentifyProvider.provideIdentify();
        assertEquals(identify.getRepositoryName(), REPOSITORY_NAME);
        assertEquals(identify.getBaseURL(), BASE_URL);
        assertEquals(identify.getProtocolVersion(), PROTOCOL_VERSION);
        assertEquals(identify.getEarliestDatestamp(), EARLIEST_DATESTAMP);
        assertEquals(identify.getDeletedRecord(), DELETED_RECORD);
        assertEquals(identify.getAdminEmail().length, 1);
        assertEquals(identify.getAdminEmail()[0], ADMIN_EMAIL);
        assertEquals(identify.getCompression().length, 1);
        assertEquals(identify.getCompression()[0], COMPRESSION);
    }
}