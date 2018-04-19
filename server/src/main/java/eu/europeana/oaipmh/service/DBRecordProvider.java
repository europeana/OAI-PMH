package eu.europeana.oaipmh.service;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.search.impl.WebMetaInfo;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.RDFMetadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.InternalServerErrorException;
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

    @Value("${enhanceWithTechnicalMetadata}")
    private boolean enhanceWithTechnicalMetadata;

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


    /**
     * Retrieves record from MongoDB and prepares EDM metadata.
     *
     * @param id identifier of the record (prefixed with ${identifierPrefix}
     * @return object of Record class which contains header (with identifier, creation date and sets) and metadata with EDM metadata
     * @throws OaiPmhException
     */
    @Override
    public Record getRecord(String id) throws OaiPmhException {
        String recordId = prepareRecordId(id);

        try {
            FullBean bean = mongoServer.getFullBean(recordId);
            if (bean != null) {
                enhanceWithTechnicalMetadata(bean);
                Header header = getHeader(id, bean);
                String edm = EdmUtils.toEDM((FullBeanImpl) bean, false);
                return new Record(header, new RDFMetadata(removeXMLHeader(edm)));
            }
        } catch (MongoDBException | MongoRuntimeException e) {
            LOG.error("Record with id " + id + " could not be retrieved.", e);
            throw new InternalServerErrorException("Record with id " + id + " could not be retrieved due to database problems.");
        }
        throw new IdDoesNotExistException(id);
    }

    private void enhanceWithTechnicalMetadata(FullBean bean) {
        if (enhanceWithTechnicalMetadata && bean != null) {
            WebMetaInfo.injectWebMetaInfo(bean, mongoServer);
        }
    }

    private String removeXMLHeader(String xml) {
        String[] split = xml.split("\\?>");
        if (split.length == 2) {
            return split[1];
        }
        return xml;
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
        if (mongoServer != null) {
            mongoServer.close();
        }
    }
}
