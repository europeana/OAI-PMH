package eu.europeana.oaipmh.service;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.jibx.*;
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    @Value("${enhanceWithTechnicalMetadata:true}")
    private boolean enhanceWithTechnicalMetadata;

    @Value("${expandWithFullText:false}")
    private boolean expandWithFullText;

    private EdmMongoServer mongoServer;

    private Set<String> fullTextIds = new HashSet<>();

    @PostConstruct
    private void init() {
        try {
            mongoServer = new EdmMongoServerImpl(host, port, recordDBName, username, password);
        } catch (MongoDBException e) {
            LOG.error("Could not connect to Mongo DB.", e);
            throw new RuntimeException(e);
        }
        loadFullTextIds();
    }

    private void loadFullTextIds() {
        if (expandWithFullText) {
            try {
                Path path = Paths.get(getClass().getClassLoader().getResource("is_fulltext.csv").toURI());
                fullTextIds.addAll(Files.readAllLines(path));
            } catch (IOException | URISyntaxException e) {
                LOG.error("Problem with loading is_fulltext.csv file.", e);
            }
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
                RDF rdf = EdmUtils.toRDF((FullBeanImpl) bean);
                expandWithFullText(rdf, recordId);
                Header header = getHeader(id, bean);
                updateDatasetName(rdf);
                String edm = EdmUtils.toEDM(rdf);
                return new Record(header, new RDFMetadata(removeXMLHeader(edm)));
            }
        } catch (MongoDBException | MongoRuntimeException e) {
            LOG.error("Record with id " + id + " could not be retrieved.", e);
            throw new InternalServerErrorException("Record with id " + id + " could not be retrieved due to database problems.");
        }
        throw new IdDoesNotExistException(id);
    }

    private void updateDatasetName(RDF rdf) {
        EuropeanaAggregationType aggregationType = rdf.getEuropeanaAggregationList().get(0);
        DatasetName dsName = new DatasetName();
        dsName.setString(aggregationType.getCollectionName().getString());
        aggregationType.setDatasetName(dsName);
        aggregationType.setCollectionName(null);
    }

    private void enhanceWithTechnicalMetadata(FullBean bean) {
        if (enhanceWithTechnicalMetadata && bean != null) {
            WebMetaInfo.injectWebMetaInfo(bean, mongoServer);
        }
    }

    /**
     * This functionality will be removed in the next version. It was introduced only for migration.
     * @param rdf rdf to expand
     */
    @Deprecated
    private void expandWithFullText(RDF rdf, String id) {
        if (expandWithFullText && fullTextIds.contains(id)) {
            // expand with full text only when this option is turned on in the configuration
            for (WebResourceType resourceType : rdf.getWebResourceList()) {
                if (resourceType.getHasMimeType() != null && resourceType.getHasMimeType().getHasMimeType().equals("application/pdf")) {
                    Type1 type = new Type1();
                    type.setResource("http://www.europeana.eu/schemas/edm/FullTextResource");
                    resourceType.setType(type);
                    break;
                }
            }
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
