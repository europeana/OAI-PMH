package eu.europeana.oaipmh.service;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClient;
import com.mongodb.event.*;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.record.api.WebMetaInfo;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.metis.mongo.dao.RecordDaoNew;
import eu.europeana.metis.schema.jibx.DatasetName;
import eu.europeana.metis.schema.jibx.EuropeanaAggregationType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.utils.ExternalRequestUtil;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Metadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.impl.ListRecordsImpl;
import eu.europeana.oaipmh.model.impl.StreamListRecords;
import eu.europeana.oaipmh.profile.TrackTime;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.InternalServerErrorException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static eu.europeana.oaipmh.service.exception.ErrorConstants.*;

public class DBRecordProvider extends BaseProvider implements RecordProvider, ConnectionPoolListener {

    private static final Logger LOG                   = LogManager.getLogger(DBRecordProvider.class);

    @Value("${mongodb.connectionUrl}")
    private String connectionUrl;

    @Value("${mongodb.record.dbname}")
    private String recordDBName;

    @Value("${enhanceWithTechnicalMetadata:true}")
    private boolean enhanceWithTechnicalMetadata;

    @Value("${expandWithFullText:false}")
    private boolean expandWithFullText;

    // for some reason we always get 2 connections directly after start-up that are not registered by the ConnectionPoolListener
    private int nrConnections = 2;

    private MongoClient mongoClient;
    private RecordDaoNew recordDao;


    @PostConstruct
    private void init() {
        initMongo();
    }

    private void initMongo() {
        // We add a connectionPoolListener so we can keep track of the number of connections
        // MongoClientOptions.Builder clientOptions = new MongoClientOptions.Builder().addConnectionPoolListener(this);
        this.mongoClient = MongoClientProvider.create(connectionUrl).createMongoClient();
        this.recordDao = new RecordDaoNew(mongoClient, recordDBName, false);
        LOG.info("Connected to mongo database {} at {}", recordDBName, new MongoClientURI(connectionUrl).getHosts());
    }

    @Override
    public void connectionPoolOpened(ConnectionPoolOpenedEvent connectionPoolOpenedEvent) {
        LOG.debug("Connection pool opened {}", connectionPoolOpenedEvent);
    }

    @Override
    public void connectionPoolClosed(ConnectionPoolClosedEvent connectionPoolClosedEvent) {
        LOG.debug("Connection pool closed {}", connectionPoolClosedEvent);
    }

    @Override
    public void connectionCheckedOut(ConnectionCheckedOutEvent connectionCheckedOutEvent) {
        // ignore
    }

    @Override
    public void connectionCheckedIn(ConnectionCheckedInEvent connectionCheckedInEvent) {
        // ignore
    }

    @Override
    public synchronized void connectionAdded(ConnectionAddedEvent connectionAddedEvent) {
        nrConnections++;
        LOG.debug("{} for dbProvider {}, total Mongo connections = {}", connectionAddedEvent, this.hashCode(), nrConnections);
    }

    @Override
    public synchronized void connectionRemoved(ConnectionRemovedEvent connectionRemovedEvent) {
        nrConnections--;
        LOG.debug("{} for dbProvider {}, total Mongo connections = {}", connectionRemovedEvent, this.hashCode(), nrConnections);
    }

    /**
     * Retrieves record from MongoDB and prepares EDM metadata.
     *
     * @param id identifier of the record (prefixed with ${identifierPrefix}
     * @return object of Record class which contains header (with identifier, creation date and sets) and metadata with EDM metadata
     * @throws OaiPmhException
     */
    @Override
    @TrackTime
    public Record getRecord(String id) throws OaiPmhException {
        String recordId = prepareRecordId(id);

        try {
            Optional<FullBean> opt = recordDao.getRecord(recordId);
            if ( opt.isEmpty() ) { return null; }

            FullBean bean = opt.get();
            return new Record(getHeader(id, bean), new Metadata(bean));
        } catch (EuropeanaException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Override
    public void checkRecordExists(String id) throws OaiPmhException {
        String recordId = prepareRecordId(id);

        try {
            if ( recordDao.hasRecord(recordId) ) { return; }
            throw new IdDoesNotExistException(msg(ID_DOES_NOT_EXIST_MSG, id));
        } catch (EuropeanaException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @Override
    public ListRecords listRecords(
            List<String> identifiers
          , ResumptionToken token) throws OaiPmhException {

        long startTime = System.currentTimeMillis();

        try {
            return new StreamListRecords(
                    recordDao.getRecords(identifiers).map(t -> createRecord(t))
                  , token);
        }
        catch (EuropeanaException e) {
            throw new OaiPmhException(e);
        }
        finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ListRecords finished in {} ms.", (System.currentTimeMillis() - startTime));
            }
        }
    }

    private Header getHeader(String id, FullBean bean) throws IdDoesNotExistException {
        if (bean != null) {
            List<String> setSpec = new ArrayList<>();
            for (String setName : bean.getEuropeanaCollectionName()) {
                setSpec.add(getSetIdentifier(setName));
            }
            return new Header(id, bean.getTimestampCreated(), setSpec);
        }
        throw new IdDoesNotExistException(id);
    }

    @Override
    @PreDestroy
    public void close() {
        LOG.info("Shutting down Mongo connections...");
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    private Record createRecord(FullBean bean) {
        return new Record(
                new Header(bean.getId(), null, bean.getEuropeanaCollectionName()[0])
              , new Metadata(bean));
    }

}
