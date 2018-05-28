package eu.europeana.oaipmh.service;

import eu.europeana.corelib.definitions.jibx.CollectionName;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.RDFMetadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EdmUtils.class)
@PowerMockIgnore("javax.management.*")
@SpringBootTest
public class DBRecordProviderTest extends BaseApiTestCase {
    private static final String TEST_RECORD_ID = "http://data.europeana.eu/item/2048432/item_RAUFTMSVPDRDYE67HPNM5FL4G6VALEOJ";

    private static final String TEST_RECORD_FILENAME = "getRecordFromDB.xml";

    private static final Date TEST_RECORD_CREATE_DATE = DateConverter.fromIsoDateTime("2017-04-05T15:04:03Z");

    private static final String[] TEST_RECORD_SETS = new String[] {"2048432_Ag_DE_DDB_BINE_99900678"};

    private static final String IDENTIFIER_PREFIX_FIELD_NAME = "identifierPrefix";

    private static final String DEFAULT_IDENTIFIER_PREFIX = "http://data.europeana.eu/item";

    @Mock
    private EdmMongoServer mongoServer;

    @InjectMocks
    private DBRecordProvider recordProvider;

    @Before
    public void initTest() {
        ReflectionTestUtils.setField(recordProvider, IDENTIFIER_PREFIX_FIELD_NAME, DEFAULT_IDENTIFIER_PREFIX);
    }

    @Test
    public void getRecord() throws IOException, MongoRuntimeException, MongoDBException, OaiPmhException {
        // given
        String record = loadRecord();
        prepareTest(record);

        // when
        Record preparedRecord = prepareRecord(record);
        Record retrievedRecord = recordProvider.getRecord(TEST_RECORD_ID);

        // then
        Assert.assertNotNull(retrievedRecord);
        assertRecordEquals(retrievedRecord, preparedRecord);
    }

    private void prepareTest(String record) throws MongoDBException, MongoRuntimeException {
        FullBeanImpl bean = PowerMockito.mock(FullBeanImpl.class);
        RDF rdf = PowerMockito.mock(RDF.class);
        given(mongoServer.getFullBean(anyString())).willReturn(bean);
        PowerMockito.mockStatic(EdmUtils.class);
        given(EdmUtils.toRDF(any(FullBeanImpl.class))).willReturn(rdf);
        EuropeanaAggregationType type = PowerMockito.mock(EuropeanaAggregationType.class);
        List<EuropeanaAggregationType> types = new ArrayList<>();
        types.add(type);
        given(rdf.getEuropeanaAggregationList()).willReturn(types);
        CollectionName name = PowerMockito.mock(CollectionName.class);
        given(type.getCollectionName()).willReturn(name);
        given(name.getString()).willReturn(TEST_RECORD_SETS[0]);
        given(EdmUtils.toEDM(any(RDF.class))).willReturn(record);
        given(EdmUtils.toRDF(any(FullBeanImpl.class))).willReturn(rdf);
        given(bean.getTimestampCreated()).willReturn(TEST_RECORD_CREATE_DATE);
        given(bean.getEuropeanaCollectionName()).willReturn(TEST_RECORD_SETS);
    }

    private void assertRecordEquals(Record retrievedRecord, Record preparedRecord) {
        Header retrievedHeader = retrievedRecord.getHeader();
        Header preparedHeader = preparedRecord.getHeader();

        Assert.assertNotNull(retrievedHeader);
        Assert.assertNotNull(preparedHeader);

        Assert.assertEquals(retrievedHeader.getDatestamp(), preparedHeader.getDatestamp());
        Assert.assertEquals(retrievedHeader.getIdentifier(), preparedHeader.getIdentifier());
        Assert.assertEquals(retrievedHeader.getSetSpec(), preparedHeader.getSetSpec());

        String metadata = retrievedRecord.getMetadata().getMetadata();
        int index = metadata.indexOf("<edm:completeness>0</edm:completeness>");
        if (index != -1) {
            metadata = metadata.substring(0, index) + metadata.substring(index + "<edm:completeness>0</edm:completeness>".length());
        }
        Assert.assertEquals(metadata, preparedRecord.getMetadata().getMetadata());
    }

    private Record prepareRecord(String record) {
        Header header = new Header();
        header.setDatestamp(TEST_RECORD_CREATE_DATE);
        header.setIdentifier(TEST_RECORD_ID);
        header.setSetSpec(TEST_RECORD_SETS[0]);

        RDFMetadata metadata = new RDFMetadata(record.substring(record.indexOf("<rdf:RDF")));
        return new Record(header, metadata);
    }

    private String loadRecord() throws IOException {
        Path path = Paths.get(resDir + "/" + TEST_RECORD_FILENAME);
        return new String(Files.readAllBytes(path));
    }

    @Test
    public void listRecords() throws IOException, MongoRuntimeException, MongoDBException, OaiPmhException {
        // given
        String record = loadRecord();
        prepareTest(record);

        // when
        Record preparedRecord = prepareRecord(record);
        List<Header> headers = new ArrayList<>();
        headers.add(preparedRecord.getHeader());

        ListRecords retrievedRecords = recordProvider.listRecords(headers);

        // then
        Assert.assertNotNull(retrievedRecords);
        Assert.assertEquals(1, retrievedRecords.getRecords().size());
        assertRecordEquals(retrievedRecords.getRecords().get(0), preparedRecord);
    }
}