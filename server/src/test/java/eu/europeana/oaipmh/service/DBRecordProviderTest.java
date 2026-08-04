package eu.europeana.oaipmh.service;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.oaipmh.AbstractIntegrationIT;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Test class for verifying the functionality of the DBRecordProvider.
 * DBRecordProvider is responsible for record management within the system,
 * and this class validates its core behaviors in an integration test environment.
 *
 **/
@SpringBootTest
public class DBRecordProviderTest extends AbstractIntegrationIT {

    private static final String TEST_RECORD_ID = "/00101/00180020C7AF376F0C82A5F47CAD7BED272DF62A";

    private static RecordDao recordDao;

    @BeforeEach
    public void setUp() {
        recordDao = ((DBRecordProvider)recordProvider).getRecordDao();
        FullBean bean = getFullBean();
        recordDao.getDatastore().save(bean);
    }

    @Test
    public void testGetRecord() throws OaiPmhException {
        Record retrievedRecord = recordProvider.getRecord(TEST_RECORD_ID);
        Assertions.assertNotNull(retrievedRecord);
        Assertions.assertEquals(TEST_RECORD_ID, retrievedRecord.getHeader().getIdentifier());
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
        list.add("/142/UEDIN_214");
        list.add(TEST_RECORD_ID);
        list.add("/44/_Resource_104186463");
        list.add("/44/_Resource_104186897");

        ListRecords records = recordProvider.listRecords(list, null);
        Assertions.assertNotNull(records);
        records.close(); // close the stream
    }

    private static FullBean getFullBean() {
        final FullBeanImpl fullBean = new FullBeanImpl();
        fullBean.setEuropeanaId(new ObjectId("81eec080f582833f364dad08"));
        fullBean.setLanguage(new String[]{"Nederlands", "Vlams", "Duits", "Frans", "Spaans", "Italians", "Duits", "Portuguese"});
        fullBean.setCountry(
                new String[]{"Nederland", "België", "Duitsland", "Frankrijk", "Spanje", "Italië", "Switzerland", "Portugal"});
        fullBean.setYear(new String[]{"2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029"});
        fullBean.setAbout(TEST_RECORD_ID);
        fullBean.setType("TEXT");
        fullBean.setTimestampCreated(Date.from(Instant.parse("2022-01-26T12:35:12.00Z")));
        fullBean.setTimestampUpdated(Date.from(Instant.parse("2022-01-26T12:35:12.00Z")));
        fullBean.setProvider(new String[]{"Pro1", "Prov2", "Prov3", "Prov4", "Prov5", "Prov6", "Prov7", "Prov8"});
        fullBean.setUserTags(new String[]{"UsrTag1", "UsrTag2", "UsrTag3", "UsrTag4", "UsrTag5", "UsrTag6", "UsrTag7", "UsrTag8"});
        fullBean.setEuropeanaCollectionName(new String[]{"Collection1", "Collection2", "Collection3", "Collection4", "Collection5"});
        fullBean.setEuropeanaCompleteness(100);

        return fullBean;
    }

}