package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Component
public class ListRecordsQuery implements OAIPMHQuery {

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
        // init thread pool
        if (threads < 1) {
            threads = 1;
        }
        threadPool = Executors
                .newFixedThreadPool(threads);
    }

    @Override
    public String getVerbName() {
        return "ListIdentifiers";
    }

    @Override
    public void execute(OAIPMHServiceClient oaipmhServer) throws OaiPmhException {
        if (sets.isEmpty()) {
            execute(oaipmhServer, null);
        } else {
            for (String setName : sets) {
                execute(oaipmhServer, setName);
            }
        }
    }

    private void execute(OAIPMHServiceClient oaipmhServer, String setName) {
        long counter = 0;
        long start = System.currentTimeMillis();
        ProgressLogger logger = new ProgressLogger(-1, logProgressInterval);

        if (!useGetRecord) {
            LOG.error("ListRecords verb not supported yet!");
        }

        ListIdentifiersQuery identifiersQuery = prepareListIdentifiersQuery(setName);
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
        } catch (ExecutionException e) {
            LOG.error("Problem with task thread execution.", e);
        }

        LOG.info("ListRecords for set " + setName + " executed in " + ProgressLogger.getDurationText(System.currentTimeMillis() - start) +
                ". Harvested " + identifiers.size() + " identifiers.");
    }

    private ListIdentifiersQuery prepareListIdentifiersQuery(String set) {
        ListIdentifiersQuery query = new ListIdentifiersQuery(metadataPrefix, from, until, set, 30);
        query.initSets();
        return query;
    }
}
