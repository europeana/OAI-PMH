package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.AbstractIntegrationIT;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for verifying the functionality of the IdentifierProvider.
 * It conducts a series of tests to validate the behavior of the listIdentifiers method
 * under various conditions such as filtering by dates, sets, and pagination limits.
 * Extends the AbstractIntegrationIT to provide integration test capabilities.
 */
public class IdentifierProviderTest extends AbstractIntegrationIT {

    @Test
    public void listIdentifiers() throws OaiPmhException {
        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, null, null, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, null, null, null, 26);
    }

    @Test
    public void listIdentifiersWithSet() throws OaiPmhException {
        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, null, null, SET_1, IDENTIFIERS_PER_PAGE);
        assertResults(result, null, null, SET_1, 5);
    }

    @Test
    public void listIdentifiersFrom() throws OaiPmhException {
        Date from = DateConverter.fromIsoDateTime(DATE_3);

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, from, null, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, from, null, null, 23);
    }

    @Test
    public void listIdentifiersUntil() throws OaiPmhException {
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, null, until, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, null, until, null, 15);
    }

    @Test
    public void listIdentifiersFromUntil() throws OaiPmhException {
        Date from = DateConverter.fromIsoDateTime(DATE_1);
        Date until = DateConverter.fromIsoDateTime(DATE_3);

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, from, until, null, IDENTIFIERS_PER_PAGE);
        assertResults(result, from, until, null, 15);
    }

    @Test
    public void listIdentifiersWithEmptyResult() throws OaiPmhException {
        Date from = DateConverter.fromIsoDateTime(DATE_3);
        Date until = DateConverter.fromIsoDateTime(Instant.now().toString());

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, from, until, SET_4, IDENTIFIERS_PER_PAGE);
        assertTrue(result.isEmpty());
    }

    @Test
    public void listIdentifiersFromUntilSet() throws OaiPmhException {
        Date from = DateConverter.fromIsoDateTime(DATE_1);
        Date until = DateConverter.fromIsoDateTime(Instant.now().toString());

        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, from, until, SET_2, IDENTIFIERS_PER_PAGE);
        assertResults(result, from, until, SET_2,7);
    }

    @Test
    public void listIdentifiersInvalid() throws OaiPmhException {
        ListIdentifiers result = identifierProvider.listIdentifiers(METADATA_FORMAT, null, null, "test", IDENTIFIERS_PER_PAGE);
        assertTrue(result.isEmpty());
    }


    private void assertResults(ListIdentifiers results, Date from, Date until, String set, int identifiersExpected) {
        assertNotNull(results);
        assertNotNull(results.stream());
        assertEquals(identifiersExpected, results.stream().count());
        assertFalse(results.isEmpty());
        results.stream().forEach(header -> {
            assertNotNull(header.getIdentifier());
            Date timestamp = header.getDatestamp();
            assertNotNull(timestamp);
            if (from != null) {
                assertTrue(timestamp.equals(from) || timestamp.after(from));
            }
            if (until != null) {
                assertTrue(timestamp.before(until) || timestamp.equals(until));
            }
            if (set != null) {
                assertEquals(1, header.getSetSpec().size());
                assertTrue(header.getSetSpec().get(0).equals(set));
            }
        });
        ResumptionToken token = results.getResumptionToken();
        if (token != null) {
            assertNotNull(token.getValue());
            assertTrue(token.getCursor() >= 0 && token.getCursor() < token.getCompleteListSize());
            assertNotNull(token.getExpirationDate());
        }
    }

}

