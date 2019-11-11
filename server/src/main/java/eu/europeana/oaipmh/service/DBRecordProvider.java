package eu.europeana.oaipmh.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
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
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

@ConfigurationProperties
public class DBRecordProvider extends BaseProvider implements RecordProvider {

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

    @Value("${threadsCount:1}")
    private int threadsCount;

    @Value("${maxThreadsCount:20}")
    private int maxThreadsCount;

    private ExecutorService threadPool;
    private EdmMongoServer mongoServer;

    @PostConstruct
    private void init() throws InternalServerErrorException {
        initMongo();
        initThreadPool();
    }

    private void initMongo() throws InternalServerErrorException {
        try {
            MongoClientOptions.Builder options = MongoClientOptions.builder();
            options.connectTimeout(5000);
            options.socketTimeout(45000);
            MongoClient client = new MongoClient(new MongoClientURI(connectionUrl, options));

            mongoServer = new EdmMongoServerImpl(client, recordDBName);
            LOG.info("Connected to mongo database {}", recordDBName);
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
        threadPool = Executors
                .newFixedThreadPool(threadsCount);
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
                    throw new RuntimeException(e);
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
        return EdmUtils.toEDM(rdf);
    }

    @TrackTime
    private RDF getRDF(FullBeanImpl bean) {
        return EdmUtils.toRDF(bean);
    }

    @Override
    public ListRecords listRecords(List<Header> identifiers) throws OaiPmhException {
        long start = System.currentTimeMillis();

        List<Record> records = new ArrayList<>(identifiers.size());

        // split identifiers into several threads
        List<Future<CollectRecordsResult>> results;
        List<Callable<CollectRecordsResult>> tasks = new ArrayList<>();

        float perThread = (float) identifiers.size() / (float) threadsCount;

        // create task for each thread
        for (int i = 0; i < threadsCount; i++) {
            tasks.add(new CollectRecordsTask(identifiers.subList((int) (i * perThread), (int) ((i + 1) * perThread)), i));
        }

        try {
            // invoke a separate thread for each provider
            results = threadPool.invokeAll(tasks);

            CollectRecordsResult collectRecordsResult;
            for (Future<CollectRecordsResult> result : results) {
                collectRecordsResult = result.get();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Thread {} collected {} records."
                            , collectRecordsResult.getThreadId(), collectRecordsResult.getRecords().size());
                }
                records.addAll((int) (collectRecordsResult.getThreadId() * perThread), collectRecordsResult.getRecords());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Thread interrupted.", e);
        } catch (ExecutionException e) {
            // I'm not sure why but on my local machine LOG.error doesn't work (only at this point?) and e.printStackTrace
            // does work so I'm keeping this for debugging purposes.
            e.printStackTrace();
            String msg = "Error retrieving data";
            LOG.error(msg, e);
            throw new InternalServerErrorException(msg);
        }

        if (records.isEmpty()) {
            throw new NoRecordsMatchException("No records found!");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("ListRecords using {} threads finished in {} ms.",
                      threadsCount, (System.currentTimeMillis() - start));
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
            LOG.debug("Technical metadata injected in {} ms.", String.valueOf(System.currentTimeMillis() - start));
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
    public void close() {
        if (mongoServer != null) {
            mongoServer.close();
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

    private class CollectRecordsResult {
        int threadId;
        List<Record> records;
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
