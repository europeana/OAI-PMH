package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.response.ListRecordsResponse;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Component
public class ListRecordsQuery extends BaseQuery implements OAIPMHQuery {

    private static final Logger LOG = LogManager.getLogger(ListRecordsQuery.class);

    @Value("${ListRecords.metadataPrefix}")
    private String metadataPrefix;

    @Value("${ListRecords.from}")
    private String from;

    @Value("${ListRecords.until}")
    private String until;

    @Value("${ListRecords.set}")
    private String set;

    @Value("${ListRecords.useGetRecord}")
    private boolean useGetRecord;

    @Value("${ListRecords.threads}")
    private int threads;

    private ExecutorService threadPool;

    @Value("${LogProgress.interval}")
    private Integer logProgressInterval;

    private List<String> sets = new ArrayList<>();

    @PostConstruct
    public void initSets() {
        if (set != null && !set.isEmpty()) {
            sets.addAll(Arrays.asList(set.split(",")));
        }
    }

    private void initThreadPool() {
        // init thread pool
        if (threads < 1) {
            threads = 1;
        }
        threadPool = Executors
                .newFixedThreadPool(threads);
    }

    @Override
    public String getVerbName() {
        return useGetRecord ? "ListIdentifiers" : "ListRecords";
    }

    @Override
    public void execute(OAIPMHServiceClient oaipmhServer) throws OaiPmhException {
        if (sets.isEmpty()) {
            execute(oaipmhServer, null);
        } else {
            for (String setIdentifier : sets) {
                execute(oaipmhServer, setIdentifier);
            }
        }
    }

    private void execute(OAIPMHServiceClient oaipmhServer, String setIdentifier) {
        if (useGetRecord) {
            executeMultithreadListRecords(oaipmhServer, setIdentifier);
        } else {
            executeListRecords(oaipmhServer, setIdentifier);
        }
    }

    private void executeMultithreadListRecords(OAIPMHServiceClient oaipmhServer, String setIdentifier) {
        initThreadPool();

        long counter = 0;
        long start = System.currentTimeMillis();
        ProgressLogger logger = new ProgressLogger(-1, logProgressInterval);

        ListIdentifiersQuery identifiersQuery = prepareListIdentifiersQuery(setIdentifier);
        List<String> identifiers = identifiersQuery.getIdentifiers(oaipmhServer);
        logger.setTotalItems(identifiers.size());

        List<Future<ListRecordsResult>> results = null;
        List<Callable<ListRecordsResult>> tasks = new ArrayList<>();

        int perThread = identifiers.size() / threads;

        // create task for each resource provider
        for (int i = 0; i < threads; i++) {
            int fromIndex = i * perThread;
            int toIndex = (i + 1) * perThread;
            if (i == threads - 1) {
                toIndex = identifiers.size();
            }
            tasks.add(new ListRecordsExecutor(identifiers.subList(fromIndex, toIndex), metadataPrefix, oaipmhServer, logProgressInterval));
        }

        try {
            // invoke a separate thread for each provider
            results = threadPool.invokeAll(tasks);

            ListRecordsResult listRecordsResult;
            for (Future<ListRecordsResult> result : results) {
                listRecordsResult = result.get();
                LOG.info("Executor finished with " + listRecordsResult.getErrors() + " errors in " + listRecordsResult.getTime() + " sec.");
                counter += perThread;
                logger.logProgress(counter);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted.", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            LOG.error("Problem with task thread execution.", e);
        }

        clean();

        LOG.info("ListRecords for set " + setIdentifier + " executed in " + ProgressLogger.getDurationText(System.currentTimeMillis() - start) +
                ". Harvested " + identifiers.size() + " identifiers.");
    }

    private void executeListRecords(OAIPMHServiceClient oaipmhServer, String setIdentifier) {
        long counter = 0;
        long start = System.currentTimeMillis();
        ProgressLogger logger = new ProgressLogger(-1, logProgressInterval);

        String request = getRequest(oaipmhServer.getOaipmhServer(), setIdentifier);

        ListRecordsResponse response = (ListRecordsResponse) oaipmhServer.makeRequest(request, ListRecordsResponse.class);
        ListRecords responseObject = response.getListRecords();
        if (responseObject != null) {
            counter += responseObject.getRecords().size();
            if (responseObject.getResumptionToken() != null) {
                logger.setTotalItems(responseObject.getResumptionToken().getCompleteListSize());
            } else {
                logger.setTotalItems(responseObject.getRecords().size());
            }

            while (responseObject.getResumptionToken() != null) {
                request = getResumptionRequest(oaipmhServer.getOaipmhServer(), responseObject.getResumptionToken().getValue());
                response = (ListRecordsResponse) oaipmhServer.makeRequest(request, ListRecordsResponse.class);
                responseObject = response.getListRecords();
                if (responseObject == null) {
                    break;
                }
                counter += responseObject.getRecords().size();
                logger.logProgress(counter);
            }
        }

        LOG.info("ListRecords for set " + setIdentifier + " executed in " + ProgressLogger.getDurationText(System.currentTimeMillis() - start) +
                ". Harvested " + counter + " records.");
    }

    private ListIdentifiersQuery prepareListIdentifiersQuery(String setIdentifier) {
        ListIdentifiersQuery query = new ListIdentifiersQuery(metadataPrefix, from, until, setIdentifier, 30);
        query.initSets();
        return query;
    }

    private String getResumptionRequest(String oaipmhServer, String resumptionToken) {
        return getBaseRequest(oaipmhServer, getVerbName()) +
                String.format(RESUMPTION_TOKEN_PARAMETER, resumptionToken);
    }

    private String getRequest(String oaipmhServer, String setIdentifier) {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseRequest(oaipmhServer, getVerbName()));
        sb.append(String.format(METADATA_PREFIX_PARAMETER, metadataPrefix));
        if (from != null && !from.isEmpty()) {
            sb.append(String.format(FROM_PARAMETER, from));
        }
        if (until != null && !until.isEmpty()) {
            sb.append(String.format(UNTIL_PARAMETER, until));
        }
        if (set != null) {
            sb.append(String.format(SET_PARAMETER, setIdentifier));
        }

        return sb.toString();
    }

    @PreDestroy
    private void clean() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }
}
