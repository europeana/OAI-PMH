package eu.europeana.oaipmh.service;


import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.mockito.Mock;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SolrBasedProviderTestCase extends BaseApiTestCase {
    @Mock
    protected CloudSolrClient solrClient;

    protected QueryResponse getResponse(String fileName) throws IOException {

        Path path = Paths.get(resDir + "/" + fileName);
        InputStream body= new FileInputStream(path.toFile());
        NamedList<Object> result= processResponse(body, null);
        QueryResponse response = new QueryResponse();
        response.setResponse(result);
        return response;
    }

    private NamedList<Object> processResponse(InputStream body, Object o) {
        XMLResponseParser parser= new XMLResponseParser();
        NamedList<Object> result= parser.processResponse(body, "UTF-8");
        return result;
    }


}
