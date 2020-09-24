package eu.europeana.oaipmh.service;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.jibx.CollectionName;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.RDFMetadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static junit.framework.TestCase.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class DBRecordProviderTest extends BaseApiTestCase {

    private static final String TEST_RECORD_ID = "http://data.europeana.eu/item/00101/00180020C7AF376F0C82A5F47CAD7BED272DF62A";

    private static final String TEST_RECORD_FILENAME = "getRecordFromDB";

    private static final Date TEST_RECORD_CREATE_DATE = DateConverter.fromIsoDateTime("2017-04-05T15:04:03Z");

    private static final String[] TEST_RECORD_SETS = new String[] {"2048432"};

    private static final String IDENTIFIER_PREFIX_FIELD_NAME = "identifierPrefix";

    private static final String DEFAULT_IDENTIFIER_PREFIX = "http://data.europeana.eu/item";

    private EdmMongoServer mongoServer;

    private DBRecordProvider recordProvider;

    @Before
    public void initTest() {

        mongoServer = mock(EdmMongoServer.class);
        recordProvider = spy(DBRecordProvider.class);

        ReflectionTestUtils.setField(recordProvider, IDENTIFIER_PREFIX_FIELD_NAME, DEFAULT_IDENTIFIER_PREFIX);
        ReflectionTestUtils.setField(recordProvider, "mongoServer", mongoServer);
    }

    @Test
    public void getRecord() throws IOException, EuropeanaException, OaiPmhException {
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

    private void prepareTest(String record) throws EuropeanaException {
        RDF rdf = mock(RDF.class);
        EuropeanaAggregationType type = mock(EuropeanaAggregationType.class);
        CollectionName name = mock(CollectionName.class);
        FullBean bean = mock(FullBeanImpl.class);

        List<EuropeanaAggregationType> types = new ArrayList<>();
        types.add(type);

        given(mongoServer.getFullBean(anyString())).willReturn(bean);
        given(rdf.getEuropeanaAggregationList()).willReturn(types);
        given(type.getCollectionName()).willReturn(name);
        doReturn(record).when(recordProvider).getEDM(any(RDF.class));
        doReturn(rdf).when(recordProvider).getRDF(any(FullBeanImpl.class));
        given(name.getString()).willReturn(TEST_RECORD_SETS[0]);
        given(bean.getTimestampCreated()).willReturn(TEST_RECORD_CREATE_DATE);
        given(bean.getEuropeanaCollectionName()).willReturn(TEST_RECORD_SETS);

        ReflectionTestUtils.setField(recordProvider, "threadsCount", 1);
        ReflectionTestUtils.setField(recordProvider, "maxThreadsCount", 20);
        ReflectionTestUtils.invokeMethod(recordProvider, "initThreadPool");
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
        int index = metadata.indexOf("metadata='");
        if (index != -1) {
            metadata = metadata.substring(index+"metadata='".length());
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
    public void listRecords() throws IOException, EuropeanaException, OaiPmhException {
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

    @Test
    public void checkRecordExists() throws EuropeanaException, OaiPmhException {
        // given
        FullBeanImpl bean = mock(FullBeanImpl.class);
        given(mongoServer.getFullBean(anyString())).willReturn(bean);

        // when
        recordProvider.checkRecordExists(TEST_RECORD_ID);

        // then if no error is thrown everything is fine
    }

    @Test(expected = IdDoesNotExistException.class)
    public void checkRecordExistsWithWrongIdentifier() throws EuropeanaException, OaiPmhException {
        // given
        given(mongoServer.getFullBean(anyString())).willReturn(null);

        // when
        recordProvider.checkRecordExists(TEST_RECORD_ID);

        // then
        fail();
    }
}