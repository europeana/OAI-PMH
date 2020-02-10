package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.Set;
import eu.europeana.oaipmh.model.response.ListSetsResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
public class ListSetsQuery extends BaseQuery implements OAIPMHQuery  {
    private static final Logger LOG = LogManager.getLogger(ListSetsQuery.class);

    @Value("${LogProgress.interval}")
    private Integer logProgressInterval;

    @Value("${ListRecords.metadataPrefix}")
    private String metadataPrefix;

    @Value("${ListSets.useListRecord}")
    private boolean useListRecord;

    @Value("${saveToFile}")
    private String saveToFile;

    @Value("${saveToFolder}")
    private String directoryLocation;

    @Value("${ListSets.threads}")
    private int threads;

    private ExecutorService threadPool;


    private void initThreadPool() {
        // init thread pool
        if (threads < 1) {
            threads = 1;
        }
        threadPool = Executors
                .newFixedThreadPool(threads);
    }

    public ListSetsQuery() {
    }

    public ListSetsQuery(int logProgressInterval) {
        this.logProgressInterval = logProgressInterval;
    }

    @Override
    public String getVerbName(){ return useListRecord ? "ListRecords" : "ListSets";
    }

    @Override
    public void execute(OAIPMHServiceClient oaipmhServer) {
        if(useListRecord) {
            executeMultithreadListRecords(oaipmhServer);
        }
        else {
            execute(oaipmhServer, null);
         }

    }

    public List<String> getSets(OAIPMHServiceClient oaipmhServer) {
        List<String> setsFromListSets = new ArrayList<>();
        execute(oaipmhServer, setsFromListSets );
        return setsFromListSets;
    }

    private void executeMultithreadListRecords(OAIPMHServiceClient oaipmhServer) {
        initThreadPool();

        long counter = 0;
        long start = System.currentTimeMillis();
        ProgressLogger logger = new ProgressLogger(-1, logProgressInterval);

        ListSetsQuery setsQuery = new ListSetsQuery(logProgressInterval);
        List<String> sets =  setsQuery.getSets(oaipmhServer);

        logger.setTotalItems(sets.size());

        List<Future<ListRecordsResult>> results = null;
        List<Callable<ListRecordsResult>> tasks = new ArrayList<>();

        int perThread = sets.size() / threads;

        // create task for each resource provider
        for (int i = 0; i < threads; i++) {
            int fromIndex = i * perThread;
            int toIndex = (i + 1) * perThread;
            if (i == threads - 1) {
                toIndex = sets.size();
            }
            tasks.add(new ListSetsExecutor(sets.subList(fromIndex, toIndex), metadataPrefix, directoryLocation, saveToFile, oaipmhServer, logProgressInterval));
        }
        try {
            // invoke a separate thread for each provider
            results = threadPool.invokeAll(tasks);

            ListRecordsResult listRecordsResult;
            for (Future<ListRecordsResult> result : results) {
                listRecordsResult = result.get();
                LOG.info("Executor finished with {} errors in {} sec.",
                        listRecordsResult.getErrors(), listRecordsResult.getTime());
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

        LOG.info("ListRecords for all sets executed in " + ProgressLogger.getDurationText(System.currentTimeMillis() - start) +
                ". Harvested " + sets.size() + " sets.");
    }


    private void execute(OAIPMHServiceClient oaipmhServer, List<String> setsFromListSet) {
        long counter = 0;
        long start = System.currentTimeMillis();
        ProgressLogger logger = new ProgressLogger(-1, logProgressInterval);

        String request = getRequest(oaipmhServer.getOaipmhServer());

        ListSetsResponse response = (ListSetsResponse) oaipmhServer.makeRequest(request, ListSetsResponse.class);
        ListSets responseObject = response.getListSets();

        if (responseObject != null) {
            counter += responseObject.getSets().size();
            if (responseObject.getResumptionToken() != null) {
                logger.setTotalItems(responseObject.getResumptionToken().getCompleteListSize());
            } else {
                logger.setTotalItems(responseObject.getSets().size());
            }
             collectSets(responseObject.getSets(), setsFromListSet);

            while (responseObject.getResumptionToken() != null) {
                request = getResumptionRequest(oaipmhServer.getOaipmhServer(), responseObject.getResumptionToken().getValue());
                response = (ListSetsResponse) oaipmhServer.makeRequest(request, ListSetsResponse.class);
                responseObject = response.getListSets();
                if (responseObject == null) {
                    break;
                }
                counter += responseObject.getSets().size();
                logger.logProgress(counter);
                collectSets(responseObject.getSets(), setsFromListSet);
            }
        }

        LOG.info("ListSet  executed in " + ProgressLogger.getDurationText(System.currentTimeMillis() - start) +
                ". Harvested " + counter + " sets.");
    }

    private void collectSets(List<Set> sets , List<String> setsFromListSet) {
        if (setsFromListSet != null) {
            for (Set set : sets) {
                setsFromListSet.add(set.getSetSpec());
            }
        }
    }

    private String getResumptionRequest(String oaipmhServer, String resumptionToken) {
        return getBaseRequest(oaipmhServer, getVerbName()) +
                String.format(RESUMPTION_TOKEN_PARAMETER, resumptionToken);
    }

    private String getRequest(String oaipmhServer) {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseRequest(oaipmhServer, getVerbName()));
        return sb.toString();
    }

    @PreDestroy
    private void clean() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }
}
