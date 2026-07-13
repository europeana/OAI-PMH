package test;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mongodb.client.MongoClients;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Metadata;
import eu.europeana.oaipmh.model.impl.GetRecordImpl;
import eu.europeana.oaipmh.model.impl.StreamListRecords;
import eu.europeana.oaipmh.model.serialize.SerializationHandler;
import eu.europeana.oaipmh.model.serialize.ServerSerializationProvider;
import eu.europeana.oaipmh.model.Record;


public class TestRecordDao {
    
    static {
        SerializationHandler.register(new ServerSerializationProvider());
    }

    private static XmlMapper xmlMapper = SerializationHandler.getSerialization();

    private RecordDao recordDao;

    public TestRecordDao() {
    }

    private static <O> O readTestExample(Class<O> c) throws IOException {
        String name = c.getSimpleName();
        InputStream is = c.getResourceAsStream("/example/" + name + ".xml");
        return ( is == null ? null : xmlMapper.readValue(is, c) );
    }

    private static <O> void testClass(Class<O> c) throws IOException {
        O obj = readTestExample(c);
        if ( obj == null ) { return; }
        xmlMapper.writeValue(System.out, obj);
    }

    private void testSingleRecord() throws Exception {
        Optional<FullBean> opt = recordDao.getRecord("/142/UEDIN_214");
        if ( opt.isEmpty() ) { return; }

        GetRecord record = new GetRecordImpl(new Record(new Header("http://data.europeana.eu/item/142/UEDIN_214", null, "2021672" )
                                                      , new Metadata(opt.get())));
        System.out.println();
        xmlMapper.writeValue(System.out, record);
    }

    private Record createRecord(FullBean bean) {
        return new Record(
                new Header(bean.getId(), null, bean.getEuropeanaCollectionName()[0])
              , new Metadata(bean));
    }

    private List<String> getIds() throws IOException {
        ListIdentifiers list = readTestExample(ListIdentifiers.class);
        return list.stream().limit(20).map(t -> t.getIdentifier().replace("http://data.europeana.eu/item", "")).toList();
    }

    private List<String> getId() {
        List<String> list = new ArrayList<String>();
        list.add("/142/UEDIN_214");
        list.add("/2021672/resource_document_mauritshuis_1");
        return list;
    }

    private void testMultipleRecords(PrintStream ps) throws Exception {

        ListRecords listRecords = new StreamListRecords(recordDao.getRecords(getIds()).map(t -> createRecord(t)), null);
        System.out.println();
        long time = System.currentTimeMillis();
        xmlMapper.writeValue(ps, listRecords);
        System.out.println(System.currentTimeMillis()-time);
    }

    public static final void main(String[] args) throws Exception {
        try ( PrintStream ps = new PrintStream(new File("C:\\Work\\incoming\\oai\\listRecords.xml")) ) {
            new TestRecordDao().testMultipleRecords(ps);
        }
    }
}
