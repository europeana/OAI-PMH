package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for validating the functionality of the {@code IdentifierProvider}.
 * This class contains unit tests for different scenarios related to listing
 * identifiers using the Open Archives Initiative Protocol for Metadata Harvesting (OAI-PMH).
 * The tests ensure that the {@code listIdentifiers} method correctly processes multiple
 * combinations of parameters and handles responses from the Solr server appropriately.
 *
 * This class extends {@code SolrBasedProviderTestCase}, which provides
 * the required test setup and shared functionality.
 */
public class IdentifierProviderTest extends SolrServiceTestCase {

    @Test
    public void listIdentifiers() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, null, null, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, null, null, null);
    }

    @Test
    public void listIdentifiersWithSet() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_SET);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, null, null, SET_1, IDENTIFIERS_PER_PAGE);
        assertResults(result, null, null, SET_1);
    }

    @Test
    public void listIdentifiersFrom() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_FROM);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date from = DateConverter.fromIsoDateTime(DATE_1);

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, from, null, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, from, null, null);
    }

    @Test
    public void listIdentifiersUntil() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_UNTIL);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date until = DateConverter.fromIsoDateTime(DATE_1);

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, null, until, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, null, until, null);
    }

    @Test
    public void listIdentifiersFromUntil() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_FROM_UNTIL);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date from = DateConverter.fromIsoDateTime(DATE_2);
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, from, until, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, from, until, null);
    }

    @Test
    public void listIdentifiersWithEmptyResult() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = Mockito.mock(QueryResponse.class);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Mockito.when(response.getResults()).thenReturn(new SolrDocumentList());
        Date from = DateConverter.fromIsoDateTime(DATE_1);
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, from, until, SET_2, IDENTIFIERS_PER_PAGE);
        assertTrue(result.isEmpty());
    }

    @Test
    public void listIdentifiersFromUntilSet() throws OaiPmhException, IOException, SolrServerException {
        QueryResponse response = getResponse(LIST_IDENTIFIERS_FROM_UNTIL_SET);
        Mockito.when(solrClient.query(Mockito.any(SolrParams.class))).thenReturn(response);
        Date from = DateConverter.fromIsoDateTime(DATE_1);
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, from, until, SET_2, IDENTIFIERS_PER_PAGE);
        assertResults(result, from, until, SET_2);
    }
}

