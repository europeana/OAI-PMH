package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.AbstractIntegrationIT;
import eu.europeana.oaipmh.service.exception.BadArgumentException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SolrServiceTest extends AbstractIntegrationIT {

    @Autowired
    SolrService solrService;


    @Test
    @Order(1)
    void testSolr() {
        SolrQuery query = new SolrQuery();
        query.setQuery("*");
        QueryResponse response = solrService.executeQuery(query);
        assertEquals(26, response.getResults().getNumFound());
    }

    @Test
    @Order(2)
    void testExceptionWithWrongQuery() {
        SolrQuery query = new SolrQuery();
        query.setQuery("title:[");
        BadArgumentException ex = assertThrows(BadArgumentException.class, ()
                ->  solrService.executeQuery(query));

        assertTrue(ex.getMessage().contains("SyntaxError: Cannot parse 'title"));
    }

    @Test
    @Order(3)
    void testClose(){
        solrService.close();
    }
}
