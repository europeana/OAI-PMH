package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.AbstractIntegrationIT;
import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the DefaultSetsProvider class, which is responsible for OAI-PMH set management.
 * This test class validates various scenarios for listing sets, including filtering by date ranges
 * and handling resumption tokens.
 *
 * The class extends AbstractIntegrationIT to leverage the integration setup for testing, such as
 * configured data sources and injected service beans.
 */
public class DefaultSetsProviderTest extends AbstractIntegrationIT {

    @Test
    public void listSets() throws OaiPmhException {
        ListSets result =  setsProvider.listSets(null, null);
        assertResults(result);
    }

    @Test
    public void listSetsFrom() throws OaiPmhException {
        Date from = DateConverter.fromIsoDateTime(DATE_1);
        ListSets result = setsProvider.listSets(from, null);
        assertResults(result);
    }

    @Test
    public void listSetsUntil() throws OaiPmhException {
        Date until = DateConverter.fromIsoDateTime(DATE_2);
        ListSets result = setsProvider.listSets(null, until);
        assertResults(result);
    }

    @Test
    public void listSetsWithResumptionToken() throws OaiPmhException {
        Date from = DateConverter.fromIsoDateTime(DATE_1);
        ListSets result = setsProvider.listSets(from, null);
        assertNotNull(result.getResumptionToken());

        result = setsProvider.listSets(result.getResumptionToken());
        assertResults(result);
    }

    private void assertResults(ListSets results) {
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
}