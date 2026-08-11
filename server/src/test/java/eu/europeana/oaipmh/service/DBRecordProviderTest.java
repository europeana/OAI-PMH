package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.AbstractIntegrationIT;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for verifying the functionality of the DBRecordProvider.
 * DBRecordProvider is responsible for record management within the system,
 * and this class validates its core behaviors in an integration test environment.
 *
 **/
@SpringBootTest
public class DBRecordProviderTest extends AbstractIntegrationIT {

    @Test
    public void testGetRecord() throws OaiPmhException {
        Record retrievedRecord = recordProvider.getRecord(RECORD_ID_2064125_1);
        Assertions.assertNotNull(retrievedRecord);
        Assertions.assertEquals(RECORD_ID_2064125_1, retrievedRecord.getHeader().getIdentifier());
        Assertions.assertNotNull(retrievedRecord.getMetadata().getMetadata());
    }

    @Test
    public void testCheckRecordExists() throws OaiPmhException {
        Record retrievedRecord = recordProvider.getRecord("test_invalid_id");
        Assertions.assertNull(retrievedRecord);
    }

    @Test
    public void testListRecords() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add(RECORD_ID_401_1);
        list.add(RECORD_ID_2064125_1);
        list.add(RECORD_ID_876_1);
        list.add(RECORD_ID_08506_7);

        ListRecords records = recordProvider.listRecords(list, null);
        Assertions.assertNotNull(records);
        records.close(); // close the stream
    }

}