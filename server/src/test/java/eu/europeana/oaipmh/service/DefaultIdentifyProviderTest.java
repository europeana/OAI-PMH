package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.AbstractIntegrationIT;
import eu.europeana.oaipmh.model.Identify;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

/**
 * Test class for verifying the functionality of the DefaultIdentifyProvider implementation.
 * Extends the AbstractIntegrationIT to leverage the integration testing setup and resources.
 *
 * The test ensures that the DefaultIdentifyProvider's provideIdentify method correctly returns an Identify object
 * with expected data properties populated.
 */
public class DefaultIdentifyProviderTest extends AbstractIntegrationIT {


    @Test
    public void provideIdentify() throws IOException, SolrServerException {
//        QueryResponse response = getResponse(EARLIEST_TIMESTAMP);
//        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        Identify identify = identifyProvider.provideIdentify();
        assertNotNull(identify);
        assertEquals(REPOSITORY_NAME, identify.getRepositoryName());
        assertEquals(BASE_URL, identify.getBaseURL());
        assertEquals(PROTOCOL_VERSION, identify.getProtocolVersion());
        assertEquals(EARLIEST_DATESTAMP, identify.getEarliestDatestamp());
        assertEquals(DELETED_RECORD, identify.getDeletedRecord());
        assertEquals(1, identify.getAdminEmail().length);
        assertEquals(ADMIN_EMAIL, identify.getAdminEmail()[0]);
        assertEquals(1, identify.getCompression().length);
        assertEquals(COMPRESSION, identify.getCompression()[0]);
    }
}