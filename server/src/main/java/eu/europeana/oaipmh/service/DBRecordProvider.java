package eu.europeana.oaipmh.service;

import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClient;
import com.mongodb.event.*;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.dao.RecordDao;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Metadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.impl.StreamListRecords;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.InternalServerErrorException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static eu.europeana.oaipmh.service.exception.ErrorConstants.*;

/**
 * Provides functionality to interact with a MongoDB database for retrieving and managing records.
 * Implements the {@link RecordProvider} interface, which facilitates OAI-PMH record operations,
 * and the {@link ConnectionPoolListener} interface to manage MongoDB connection pool events.
 * Extends {@link BaseProvider} for common utility methods.
 */
public class DBRecordProvider extends BaseProvider implements RecordProvider, ConnectionPoolListener {

    private static final Logger LOG                   = LogManager.getLogger(DBRecordProvider.class);

    // for some reason we always get 2 connections directly after start-up that are not registered by the ConnectionPoolListener
    private int nrConnections = 2;

    private MongoClient mongoClient;
    private RecordDao recordDao;

    @PostConstruct
    private void init() {
        initMongo();
    }

    /**
     * Initializes the MongoDB connection and sets up required DAOs.
     * This method creates a MongoClient instance using the specified
     * connection URL and initializes the {@code RecordDao} object
     * for database operations. It also logs the connection details.
     *
     * Note:
     * - This method uses a connection pool listener to monitor the
     *   number of active connections in the MongoDB connection pool.
     */
    private void initMongo() {
        this.mongoClient = MongoClientProvider.create(settings.getConnectionUrl()).createMongoClient();
        this.recordDao = new RecordDao(mongoClient, settings.getRecordDBName(), false);
        LOG.info("Connected to mongo database {} at {}", settings.getRecordDBName(), new MongoClientURI(settings.getConnectionUrl()).getHosts());
    }

    @Override
    public void connectionPoolCreated(ConnectionPoolCreatedEvent connectionPoolOpenedEvent) {
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
    public synchronized void connectionCreated(ConnectionCreatedEvent connectionAddedEvent) {
        nrConnections++;
        LOG.debug("{} for dbProvider {}, total Mongo connections = {}", connectionAddedEvent, this.hashCode(), nrConnections);
    }

    @Override
    public synchronized void connectionClosed(ConnectionClosedEvent connectionRemovedEvent) {
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
    public Record getRecord(String id) throws OaiPmhException {
        String recordId = prepareRecordId(id);
        try {
            Optional<FullBean> opt = recordDao.getRecord(recordId);
            if ( opt.isEmpty() ) { return null; }

            FullBean bean = opt.get();
            return new Record(getHeader(id, bean), new Metadata(bean));
        } catch (RuntimeException  e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    /**
     * Verifies the existence of a record based on the provided identifier and throws an exception if it does not exist.
     *
     * This method transforms the given identifier into the appropriate record ID format using the {@code prepareRecordId}
     * method, then checks for its existence in the database via the {@code recordDao} object. If the record does not exist,
     * an {@code IdDoesNotExistException} is thrown. If any unexpected runtime error occurs during the operation,
     * an {@code InternalServerErrorException} is raised.
     *
     * @param id the identifier of the record to check
     * @throws OaiPmhException if the record does not exist or if an unexpected error is encountered
     */
    @Override
    public void checkRecordExists(String id) throws OaiPmhException {
        String recordId = prepareRecordId(id);
        try {
            if ( recordDao.hasRecord(recordId) ) { return; }
            throw new IdDoesNotExistException(msg(ID_DOES_NOT_EXIST_MSG, id));
        } catch (RuntimeException  e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    /**
     * Retrieves a list of records based on provided identifiers and an optional resumption token.
     *
     * @param identifiers a list of unique record identifiers used to fetch specific records.
     * @param token an optional resumption token for paginated retrieval of records.
     * @return a ListRecords instance containing the retrieved records and information about pagination.
     * @throws OaiPmhException if an unexpected error occurs during the retrieval process.
     */
    @Override
    public ListRecords listRecords(List<String> identifiers,
                                   ResumptionToken token) throws OaiPmhException {
        long startTime = System.currentTimeMillis();
        List<String> preparedIdentifiers = identifiers.stream().map(this::prepareRecordId).toList();
        try {
            Stream<FullBean> stream = recordDao.getRecords(preparedIdentifiers);
            return new StreamListRecords(
                    stream.map(t -> createRecord(t)),
                    token
            );
        }
        catch (RuntimeException e) {
            throw new OaiPmhException(e);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ListRecords finished in {} ms.", (System.currentTimeMillis() - startTime));
            }
        }
    }

    /**
     * Creates a {@code Header} object based on the provided identifier and {@code FullBean}.
     *
     * The method extracts collection names and timestamps from the given {@code FullBean}
     * to construct a {@code Header} containing the identifier, creation timestamp, and a
     * list of set specifications. If the {@code FullBean} is {@code null}, or if the
     * identifier does not exist, an {@code IdDoesNotExistException} is thrown.
     *
     * @param id the unique identifier for the record
     * @param bean the {@code FullBean} containing metadata to construct the {@code Header};
     *             includes collection names and creation timestamp
     * @return a {@code Header} instance populated with the identifier, creation timestamp,
     *         and list of set specifications
     * @throws IdDoesNotExistException if the {@code id} does not correspond to an existing record
     */
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

    /**
     * Creates a new {@code Record} object using the provided {@code FullBean}.
     *
     * The method constructs a {@code Record} with a {@code Header} and {@code Metadata} initialized
     * based on the data from the {@code FullBean} parameter. The {@code Header} is created using
     * the ID, the first entry of the Europeana Collection Name array, and a null timestamp.
     * The {@code Metadata} is initialized with the {@code FullBean}.
     *
     * @param bean the {@code FullBean} containing data for constructing the {@code Record}.
     *             It provides the ID, collection name, and other relevant metadata.
     *
     * @return a {@code Record} instance containing a populated {@code Header} and {@code Metadata}.
     */
    private Record createRecord(FullBean bean) {
        return new Record(
                new Header(bean.getId(), null, bean.getEuropeanaCollectionName()[0])
                , new Metadata(bean));
    }
}

