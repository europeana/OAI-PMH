package eu.europeana.oaipmh.service;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.BeforeClass;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SolrBasedProviderTest extends BaseApiTest {
    @Mock
    protected CloudSolrClient solrClient;

    protected QueryResponse getResponse(String fileName) throws IOException {
        Path path = Paths.get(resDir + "/" + fileName);
        byte[] bytes = Files.readAllBytes(path);
        QueryResponse response = (QueryResponse) QueryResponse.deserialize(bytes);
        return response;
    }
}
