package eu.europeana.oaipmh.service;

import eu.europeana.api.commons_sb3.auth.apikey.ApikeyBasedAuthentication;
import eu.europeana.oaipmh.config.OaiPmhSettings;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Unit tests for the {@link RecordApi} class.
 * This test class is responsible for validating the behavior of the RecordApi in various scenarios.
 * It includes tests for exception handling, null input validation, and API response validation.
 *
 * An API key-based authentication mechanism is used for interacting with the Record API,
 * with a mock setup for dependent settings and serialization in some test cases.
 * Annotations:
 * - {@code @Disabled}: Marks some tests as disabled as we need now real time apikey or token mechanisam.
 *                     as we don't use the RecordAPi to fetch records, few tests are disabled.
 */
@SpringBootTest
public class RecordApiTest {

    private static final String TEST_RECORD_ID = "/00101/00180020C7AF376F0C82A5F47CAD7BED272DF62A";

    private static RecordApi recordApi;

    private static OaiPmhSettings settings;

    @BeforeAll
    static void setup() {
        recordApi  = new RecordApi(new ApikeyBasedAuthentication(""));
        settings = Mockito.mock(OaiPmhSettings.class);
        ReflectionTestUtils.setField(recordApi, "settings", settings);
        Mockito.when(settings.getRecordApiUrl()).thenReturn("https://api.europeana.eu/record");
    }

    @Test
    void shouldThrowException() {
        RecordApi recordApi = new RecordApi(new ApikeyBasedAuthentication("invalid_api_key"));

        OaiPmhSettings settings = Mockito.mock(OaiPmhSettings.class);
        ReflectionTestUtils.setField(recordApi, "settings", settings);
        Mockito.when(settings.getRecordApiUrl()).thenReturn("https://api.europeana.eu/record");

        OaiPmhException ex = assertThrows(OaiPmhException.class, ()
                -> recordApi.getRecord(TEST_RECORD_ID));

        assertEquals("API key is not valid", ex.getMessage());
    }

    @Disabled
    @Test
    void shouldThrowInvalidIdException() {
        IdDoesNotExistException ex = assertThrows(IdDoesNotExistException.class, ()
                -> recordApi.getRecord("non_exsiting_id"));

        assertEquals("Record with id 'non_exsiting_id' not found", ex.getMessage());
    }

    @Test
    void shouldThrowException_ForNullId() {
        assertThrows(IdDoesNotExistException.class, ()
                -> recordApi.getRecord(null));
    }

    @Disabled
    @Test
    void getRecord() throws OaiPmhException {
        Record record = recordApi.getRecord(TEST_RECORD_ID);
        Assertions.assertNotNull(record);
        Assertions.assertEquals(TEST_RECORD_ID, record.getHeader().getIdentifier());
        Assertions.assertNotNull(record.getMetadata().getMetadata());
        Assertions.assertTrue(record.getMetadata().getMetadata().toString().contains("about=\"http://data.europeana.eu/item"+TEST_RECORD_ID));
    }

    @Disabled
    @Test
    void getRecords() throws OaiPmhException {
        ListRecords record = recordApi.listRecords(Arrays.asList(TEST_RECORD_ID), null);
        Assertions.assertNotNull(record);
        Assertions.assertEquals(record.stream().findFirst().get().getHeader().getIdentifier(), TEST_RECORD_ID);
        Assertions.assertNotNull(record.stream().findFirst().get().getMetadata().getMetadata());
    }
}
