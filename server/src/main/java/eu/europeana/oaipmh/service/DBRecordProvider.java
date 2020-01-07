package eu.europeana.oaipmh.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.event.*;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.jibx.*;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.search.impl.WebMetaInfo;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.metis.utils.ExternalRequestUtil;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.RDFMetadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.profile.TrackTime;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.InternalServerErrorException;
import eu.europeana.oaipmh.service.exception.NoRecordsMatchException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;

@Configuration
public class DBRecordProvider extends BaseProvider implements RecordProvider, ConnectionPoolListener {

    private static final Logger LOG                   = LogManager.getLogger(DBRecordProvider.class);
    private static final String RECORD_WITH_ID        = "Record with id %s ";
    private static final int    THREADS_THRESHOLD     = 10;
    private static final int    MAX_THREADS_THRESHOLD = 20;

    @Value("${mongodb.connectionUrl}")
    private String connectionUrl;

    @Value("${mongodb.record.dbname}")
    private String recordDBName;

    @Value("${enhanceWithTechnicalMetadata:true}")
    private boolean enhanceWithTechnicalMetadata;

    @Value("${expandWithFullText:false}")
    private boolean expandWithFullText;

    // number of threads from configuration
    @Value("${threadsCount:1}")
    private int threadsCount;

    // TODO figure out difference between threadsCount and maxThreadCount.
    // Looks like both are static numbers from the configuration and never changed (at least not after initialization in initThreadPool method)
    @Value("${maxThreadsCount:20}")
    private int maxThreadsCount;

    // for some reason we always get 2 connections directly after start-up that are not registered by the ConnectionPoolListener
    private int nrConnections = 2;

    private ExecutorService threadPool;
    private EdmMongoServer mongoServer;

    @PostConstruct
    private void init() throws InternalServerErrorException {
        initMongo();
        initThreadPool();
    }

    private void initMongo() throws InternalServerErrorException {
        try {
            // We add a connectionPoolListener so we can keep track of the number of connections
            MongoClientOptions.Builder clientOptions = new MongoClientOptions.Builder().addConnectionPoolListener(this);
            MongoClientURI uri = new MongoClientURI(connectionUrl, clientOptions);
            mongoServer = new EdmMongoServerImpl(new MongoClient(uri), recordDBName, false);
            LOG.info("Connected to mongo database {} at {}", recordDBName, uri.getHosts());
        } catch (MongoDBException e) {
            LOG.error("Could not connect to Mongo DB.", e);
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    /**
     * Threads count must be at least 1. When it's bigger than <code>THREADS_THRESHOLD</code> but smaller than <code>maxThreadsCount</code>
     * a warning is displayed. When it exceeds <code>maxThreadsCount</code> a warning is displayed and the value is set to <code>MAX_THREADS_THRESHOLD</code>
     */
    private void initThreadPool() {
        // init thread pool
        if (maxThreadsCount < THREADS_THRESHOLD) {
            maxThreadsCount = MAX_THREADS_THRESHOLD;
        }

        if (threadsCount < 1) {
            threadsCount = 1;
        } else if (threadsCount > THREADS_THRESHOLD && threadsCount <= maxThreadsCount) {
            LOG.warn("Number of threads exceeds " + THREADS_THRESHOLD + " which may narrow the number of clients working in parallel");
        } else if (threadsCount > maxThreadsCount) {
            LOG.warn("Number of threads exceeds {}, which may highly narrow the number of clients working in parallel. Changing to {}",
                     maxThreadsCount, MAX_THREADS_THRESHOLD);
            threadsCount = MAX_THREADS_THRESHOLD;
        }
        LOG.info("Creating new thread pool with {} threads.", threadsCount);
        threadPool = Executors.newFixedThreadPool(threadsCount);
    }

    @Override
    public void connectionPoolOpened(ConnectionPoolOpenedEvent connectionPoolOpenedEvent) {
        LOG.debug("Connection pool opened {}", connectionPoolOpenedEvent);
    }

    @Override
    public void connectionPoolClosed(ConnectionPoolClosedEvent connectionPoolClosedEvent) {
        LOG.debug("Connectio pool closed {}", connectionPoolClosedEvent);
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
    public void waitQueueEntered(ConnectionPoolWaitQueueEnteredEvent connectionPoolWaitQueueEnteredEvent) {
        // ignore
    }

    @Override
    public void waitQueueExited(ConnectionPoolWaitQueueExitedEvent connectionPoolWaitQueueExitedEvent) {
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

        FullBean bean = getFullBean(recordId);
        return new Record(getHeader(id, bean), prepareRDFMetadata(recordId, (FullBeanImpl) bean));
    }

    @Override
    public void checkRecordExists(String id) throws OaiPmhException {
        String recordId = prepareRecordId(id);

        FullBean bean = getFullBean(recordId);
        if (bean == null) {
            throw new IdDoesNotExistException("Record with identifier " + id + " not found!");
        }
    }

    @TrackTime
    private FullBean getFullBean(String recordId) throws InternalServerErrorException {
        try {
            return ExternalRequestUtil.retryableExternalRequest(() -> {
                try {
                    return mongoServer.getFullBean(recordId);
                } catch (Exception e) {
                    throw new RuntimeException("Error retrieving fullbean for record "+recordId, e);
                }
            });
        } catch (Exception e) {
            LOG.error(String.format(RECORD_WITH_ID, recordId) + " could not be retrieved.", e);
            throw new InternalServerErrorException(String.format(RECORD_WITH_ID, recordId) + " could not be retrieved due to database problems.");
        }
    }

    private RDFMetadata prepareRDFMetadata(String recordId, FullBeanImpl bean) throws OaiPmhException {
        if (bean != null) {
            enhanceWithTechnicalMetadata(bean);
            RDF rdf = getRDF(bean);
            if (rdf == null) {
                throw new InternalServerErrorException(String.format(RECORD_WITH_ID, recordId) + " could not be converted to EDM.");
            }
            updateDatasetName(rdf);
            String edm = getEDM(rdf);
            return new RDFMetadata(removeXMLHeader(edm));
        }
        throw new IdDoesNotExistException(recordId);
    }

    @TrackTime
    private String getEDM(RDF rdf) {
        try {
            return EdmUtils.toEDM(rdf);
        } catch (RuntimeException e) {
            // in the past we've had records that threw a JibX marshalling error because of missing data,
            // so we catch those to log which record fails
            String id = "unknown";
            if (!rdf.getEuropeanaAggregationList().isEmpty()) {
                id = rdf.getEuropeanaAggregationList().get(0).getAbout();
            }
            LOG.error("Error converting RDF to EDM for record {}", id);
            throw e;
        }
    }

    @TrackTime
    private RDF getRDF(FullBeanImpl bean) {
        return EdmUtils.toRDF(bean);
    }

    @Override
    public ListRecords listRecords(List<Header> identifiers) throws OaiPmhException {
        long startTime = System.currentTimeMillis();

        List<Record> records = new ArrayList<>(identifiers.size());

        // split identifiers into several threads
        List<Future<CollectRecordsResult>> results;
        List<Callable<CollectRecordsResult>> tasks = new ArrayList<>();

        // when creating threads we round off, any remaining record is added to the last created thread
        double perThread = identifiers.size() / (double) threadsCount;
        LOG.debug("{} identifiers and {} threads, so {} records per thread", identifiers.size(), threadsCount, perThread);

        // create task for each thread
        for (int i = 0; i < threadsCount; i++) {
            int start = (int) (i * perThread);
            int end = (int) ((i + 1) * perThread);
            List<Header> headers = identifiers.subList(start, end);
            LOG.debug("Creating task {} to retrieve records {} to {}", i, start, end);
            tasks.add(new CollectRecordsTask(headers, i));
        }

        try {
            // invoke a separate thread for each provider
            results = threadPool.invokeAll(tasks);

            CollectRecordsResult collectRecordsResult;
            for (Future<CollectRecordsResult> result : results) {
                collectRecordsResult = result.get();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Thread {} collected {} records.",
                            collectRecordsResult.getThreadId(), collectRecordsResult.getRecords().size());
                }
                records.addAll((int) (collectRecordsResult.getThreadId() * perThread), collectRecordsResult.getRecords());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Thread interrupted.", e);
        } catch (ExecutionException e) {
            String msg = "Error retrieving data";
            LOG.error(msg, e);
            throw new InternalServerErrorException(msg);
        }

        if (records.isEmpty()) {
            throw new NoRecordsMatchException("No records found!");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("ListRecords using {} threads finished in {} ms.", threadsCount, (System.currentTimeMillis() - startTime));
        }

        ListRecords result = new ListRecords();
        result.setRecords(records);
        return result;
    }

    @TrackTime
    private void updateDatasetName(RDF rdf) {
        EuropeanaAggregationType aggregationType = rdf.getEuropeanaAggregationList().get(0);
        if (aggregationType.getCollectionName() != null) {
            DatasetName dsName = new DatasetName();
            dsName.setString(aggregationType.getCollectionName().getString());
            aggregationType.setDatasetName(dsName);
            aggregationType.setCollectionName(null);
        }
    }

    @TrackTime
    private void enhanceWithTechnicalMetadata(FullBean bean) {
        long start = System.currentTimeMillis();
        if (enhanceWithTechnicalMetadata && bean != null) {
            WebMetaInfo.injectWebMetaInfoBatch(bean, mongoServer);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Technical metadata injected in {} ms.", String.valueOf(System.currentTimeMillis() - start));
            }
        }
    }

    @TrackTime
    private String removeXMLHeader(String xml) {
        int index = xml.indexOf("?>");
        if (index != -1) {
            return xml.substring(index + "?>".length());
        }
        return xml;
    }

    private Header getHeader(String id, FullBean bean) throws IdDoesNotExistException {
        if (bean != null) {
            Header header = new Header();
            header.setIdentifier(id);
            header.setDatestamp(bean.getTimestampCreated());
            List<String> setSpec = new ArrayList<>();
            for (String setName : bean.getEuropeanaCollectionName()) {
                setSpec.add(getSetIdentifier(setName));
            }
            header.setSetSpec(setSpec);
            return header;
        }
        throw new IdDoesNotExistException(id);
    }

    @Override
    @PreDestroy
    public void close() {
        LOG.info("Shutting down Mongo connections...");
        if (mongoServer != null) {
            mongoServer.close();
        }
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }


    private class CollectRecordsTask implements Callable<CollectRecordsResult> {

        private int threadId;

        private List<Header> identifiers;

        CollectRecordsTask(List<Header> identifiers, int threadId) {
            this.identifiers = identifiers;
            this.threadId = threadId;
            LOG.trace("Create thread {}", threadId);
        }

        @Override
        public CollectRecordsResult call() throws Exception {
            List<Record> records = new ArrayList<>();

            for (Header header : identifiers) {
                String recordId = prepareRecordId(header.getIdentifier());
                FullBean bean = getFullBean(recordId);
                records.add(new Record(header, prepareRDFMetadata(recordId, (FullBeanImpl) bean)));
            }
            return new CollectRecordsResult(threadId, records);
        }
    }

    private static class CollectRecordsResult {

        private int threadId;
        private List<Record> records;

        CollectRecordsResult(int threadId, List<Record> records) {
            this.threadId = threadId;
            this.records = records;
            LOG.trace("Thread {} returning {} records", threadId, records.size());
        }
        int getThreadId() {
            return threadId;
        }
        List<Record> getRecords() {
            return records;
        }
    }
}
