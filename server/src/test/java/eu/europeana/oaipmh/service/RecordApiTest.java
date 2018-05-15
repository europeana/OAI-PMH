package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.RDFMetadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.BDDMockito.given;

/**
 * Tests retrieving record information from the Europeana Record API
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class RecordApiTest extends BaseApiTestCase {

    private static final String TEST_RECORD_ID = "90402/BK_1978_399";

    private static final String TEST_RECORD_FILENAME = "getRecord.xml";

    @Mock
    private RecordApi recordApi;

    /**
     * Test retrieval of record xml from Europeana Record API
     * @throws OaiPmhException
     */
    @Test
    public void getRecord() throws OaiPmhException, IOException {
        Record record = new Record(null, loadRecord());
        given(recordApi.getRecord(TEST_RECORD_ID)).willReturn(record);

        Record xml = recordApi.getRecord(TEST_RECORD_ID);
        Assert.assertNotNull(xml);

        // note that this check only works for records that do not redirect to a new record Id under water
        Assert.assertTrue(xml.getMetadata().getMetadata().contains("about=\"http://data.europeana.eu/item/"+TEST_RECORD_ID));
    }

    private RDFMetadata loadRecord() throws IOException {
        Path path = Paths.get(resDir + "/" + TEST_RECORD_FILENAME);
        String content = new String(Files.readAllBytes(path));
        return new RDFMetadata(content);
    }

    /**
     * Test if the proper error is thrown if we provide an incorrect id
     * @throws OaiPmhException
     */
    @Test(expected=IdDoesNotExistException.class)
    public void getRecordNotExists() throws OaiPmhException {
        given(recordApi.getRecord("INCORRECT/ID")).willThrow(new IdDoesNotExistException("INCORRECT/ID"));

        recordApi.getRecord("INCORRECT/ID");
        Assert.assertTrue(false);
    }
}
