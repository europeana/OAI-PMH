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
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.RDFMetadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.InternalServerErrorException;
import eu.europeana.oaipmh.service.exception.NoRecordsMatchException;
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
import java.util.*;
import java.util.concurrent.*;

@ConfigurationProperties
public class DBRecordProvider extends BaseProvider implements RecordProvider {

    private static final Logger LOG = LogManager.getLogger(DBRecordProvider.class);

    private static final String RECORD_WITH_ID = "Record with id %s ";

    private static final int THREADS_THRESHOLD = 10;

    private static final int MAX_THREADS_THRESHOLD = 20;

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

    @Value("${threadsCount:1}")
    private int threadsCount;

    @Value("${maxThreadsCount:20}")
    private int maxThreadsCount;

    private ExecutorService threadPool;

    private EdmMongoServer mongoServer;

    private Set<String> fullTextIds = new HashSet<>();

    @PostConstruct
    private void init() throws InternalServerErrorException {
        initMongo();
        loadFullTextIds();
        initThreadPool();

    }

    private void initMongo() throws InternalServerErrorException {
        try {
            mongoServer = new EdmMongoServerImpl(host, port, recordDBName, username, password);
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
            LOG.warn("Number of threads exceeds " + maxThreadsCount + " which may highly narrow the number of clients working in parallel. Changing to " + MAX_THREADS_THRESHOLD);
            threadsCount = MAX_THREADS_THRESHOLD;
        }
        threadPool = Executors
                .newFixedThreadPool(threadsCount);
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
            return new Record(getHeader(id, bean), prepareRDFMetadata(recordId, (FullBeanImpl) bean));
        } catch (MongoDBException | MongoRuntimeException e) {
            LOG.error(String.format(RECORD_WITH_ID, id) + " could not be retrieved.", e);
            throw new InternalServerErrorException(String.format(RECORD_WITH_ID, id) + " could not be retrieved due to database problems.");
        }
    }

    private RDFMetadata prepareRDFMetadata(String recordId, FullBeanImpl bean) throws OaiPmhException {
        if (bean != null) {
            enhanceWithTechnicalMetadata(bean);
            RDF rdf = EdmUtils.toRDF((FullBeanImpl) bean);
            if (rdf == null) {
                throw new InternalServerErrorException(String.format(RECORD_WITH_ID, recordId) + " could not be converted to EDM.");
            }
            expandWithFullText(rdf, recordId);
            updatePreview(rdf);
            updateDatasetName(rdf);
            String edm = EdmUtils.toEDM(rdf);
            edm = injectEuropeanaCompleteness(edm, bean.getEuropeanaCompleteness());
            return new RDFMetadata(removeXMLHeader(edm));
        }
        throw new IdDoesNotExistException(recordId);
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
                LOG.info("Thread no " + collectRecordsResult.getThreadId() + " collected " + collectRecordsResult.getRecords().size() + " records.");
                records.addAll((int) (collectRecordsResult.getThreadId() * perThread), collectRecordsResult.getRecords());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted.", e);
        } catch (ExecutionException e) {
            LOG.error("Problem with task thread execution.", e);
        }

        if (records.isEmpty()) {
            throw new NoRecordsMatchException("No records found!");
        }

        LOG.info("ListRecords using " + threadsCount + " threads finished in " + (System.currentTimeMillis() - start) + " ms.");

        ListRecords result = new ListRecords();
        result.setRecords(records);
        return result;
    }

    private void updateDatasetName(RDF rdf) {
        EuropeanaAggregationType aggregationType = rdf.getEuropeanaAggregationList().get(0);
        DatasetName dsName = new DatasetName();
        dsName.setString(aggregationType.getCollectionName().getString());
        aggregationType.setDatasetName(dsName);
        aggregationType.setCollectionName(null);
    }

    /**
     * @deprecated
     *
     * This method is deprecated. It was temporarily created to include europeana completeness
     * to the resulting record metadata but without including it to EDM schema. It just searches the
     * EDM string (with RDF xml) to locate the end tag for edm:EuropeanaAggregation. After finding it it
     * injects the edm:completeness tag with the proper value just before the edm:EuropeanaAggregation closing
     * tag. This may cause that deserialization RDF from this xml will not be possible without removing
     * the edm:completeness tag first.
     *
     * @param edm edm string created from RDF object
     * @param completeness completeness value to be used inside edm:completeness tag
     * @return the changed edm string
     */
    @Deprecated
    private String injectEuropeanaCompleteness(String edm, int completeness) {
        int index = edm.indexOf("</edm:EuropeanaAggregation>");
        if (index != -1) {
            return edm.substring(0, index) + "<edm:completeness>" + completeness + "</edm:completeness>" + edm.substring(index);
        }
        return edm;
    }

    private void updatePreview(RDF rdf) {
        String resource = null;

        for (Aggregation aggregation : rdf.getAggregationList()) {
            if (aggregation.getObject() != null) {
                resource = aggregation.getObject().getResource();
            } else if (aggregation.getIsShownBy() != null) {
                resource = aggregation.getIsShownBy().getResource();
            }
            if (resource != null) {
                break;
            }
        }
        if (resource != null && !rdf.getEuropeanaAggregationList().isEmpty()) {
            EuropeanaAggregationType europeanaAggregationType = rdf.getEuropeanaAggregationList().get(0);
            Preview preview = europeanaAggregationType.getPreview();
            if (preview == null) {
                preview = new Preview();
                europeanaAggregationType.setPreview(preview);
            }
            preview.setResource(resource);
        }
    }

    private void enhanceWithTechnicalMetadata(FullBean bean) {
        if (enhanceWithTechnicalMetadata && bean != null) {
            WebMetaInfo.injectWebMetaInfo(bean, mongoServer);
        }
    }

    /**
     * @deprecated
     *
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
        private final Logger LOG = LogManager.getLogger(CollectRecordsTask.class);

        private int threadId;

        private List<Header> identifiers;

        CollectRecordsTask(List<Header> identifiers, int threadId) {
            this.identifiers = identifiers;
            this.threadId = threadId;
        }

        @Override
        public CollectRecordsResult call() throws Exception {
            List<Record> records = new ArrayList<>();

            for (Header header : identifiers) {
                try {
                    String recordId = prepareRecordId(header.getIdentifier());
                    FullBean bean = mongoServer.getFullBean(recordId);
                    records.add(new Record(header, prepareRDFMetadata(recordId, (FullBeanImpl) bean)));
                } catch (MongoDBException | MongoRuntimeException e) {
                    LOG.error(String.format(RECORD_WITH_ID, header.getIdentifier()) + " could not be retrieved.", e);
                    throw new InternalServerErrorException(String.format(RECORD_WITH_ID, header.getIdentifier()) + " could not be retrieved due to database problems.");
                }
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
        }

        int getThreadId() {
            return threadId;
        }

        List<Record> getRecords() {
            return records;
        }
    }
}
