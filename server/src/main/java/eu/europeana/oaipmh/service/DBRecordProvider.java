package eu.europeana.oaipmh.service;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@ConfigurationProperties
public class DBRecordProvider extends BaseProvider implements RecordProvider {

    private static final Logger LOG = LogManager.getLogger(DBRecordProvider.class);

    @Value("${mongo.host}")
    private String host;

    @Value("${mongo.port}")
    private String port;

    @Value("${mongo.username}")
    private String username;

    @Value("${mongo.password}")
    private String password;

    @Value("${mongo.record.dbname}")
    private String recordDBName;

    @Value("${mongo.registry.dbname}")
    private String registryDBName;

    private EdmMongoServer mongoServer;

    @PostConstruct
    private void init() {
        try {
            mongoServer = new EdmMongoServerImpl(host, port, recordDBName, username, password);
        } catch (MongoDBException e) {
            LOG.error("Could not connect to Mongo DB.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Record getRecord(String id) throws OaiPmhException {
        String recordId = prepareRecordId(id);

        try {
            FullBean bean = mongoServer.getFullBean(recordId);
            if (bean != null) {
                Header header = getHeader(id, bean);
                return new Record(header, EdmUtils.toRDF((FullBeanImpl) bean));
            }
        } catch (MongoDBException | MongoRuntimeException e) {
            LOG.error("Record with id " + id + " could not be retrieved.", e);
            throw new OaiPmhException(e.getMessage());
        }
        return null;
    }

    private Header getHeader(String id, FullBean bean) {
        Header header = new Header();
        header.setIdentifier(id);
        header.setDatestamp(bean.getTimestampCreated());
        header.setSetSpec(Arrays.asList(bean.getEuropeanaCollectionName()));
        return header;
    }

    @Override
    public void close() {

    }
}
