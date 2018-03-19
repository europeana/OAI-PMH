package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.service.RecordApi;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Tests retrieving record information from the Europeana Record API
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RecordApiTest {

    private static final String TEST_RECORD_ID = "90402/BK_1978_399";

    @Autowired
    private RecordApi recordApi;

    /**
     * Test retrieval of record xml from Europeana Record API
     * @throws OaiPmhException
     */
    @Test
    public void getRecord() throws OaiPmhException {
        String xml = recordApi.getRecord(TEST_RECORD_ID);
        Assert.assertNotNull(xml);

        // note that this check only works for records that do not redirect to a new record Id under water
        Assert.assertTrue(xml.contains("about=\"http://data.europeana.eu/item/"+TEST_RECORD_ID));
    }

    /**
     * Test if the proper error is thrown if we provide an incorrect id
     * @throws OaiPmhException
     */
    @Test(expected= IdDoesNotExistException.class)
    public void getRecordNotExists() throws OaiPmhException {
        String xml = recordApi.getRecord("INCORRECT/ID");
        Assert.assertNull(xml);
    }
}
