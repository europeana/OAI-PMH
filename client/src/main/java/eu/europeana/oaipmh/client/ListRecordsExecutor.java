package eu.europeana.oaipmh.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.Callable;

public class ListRecordsExecutor implements Callable<ListRecordsResult> {

    private static final Logger LOG = LogManager.getLogger(ListRecordsExecutor.class);

    private static final int MAX_ERRORS_PER_THREAD = 30;

    private static ProgressLogger logger = null;
    private static long loggerThreadId;
    private int logProgressInterval;

    private List<String> identifiers;

    private String metadataPrefix;

    private OAIPMHServiceClient oaipmhServer;

    public ListRecordsExecutor(List<String> identifiers, String metadataPrefix, OAIPMHServiceClient oaipmhServer, int logProgressInterval) {
        this.identifiers = identifiers;
        this.metadataPrefix = metadataPrefix;
        this.oaipmhServer = oaipmhServer;
        this.logProgressInterval = logProgressInterval;
    }

    @Override
    public ListRecordsResult call() throws Exception {
        int errors = 0;
        long counter = 0;
        long start = System.currentTimeMillis();

        // This is a bit of a hack. The first callable that reaches this point will create a progressLogger and only
        // this callable will log progress. This is to avoid too much logging from all threads.
        synchronized(this) {
            if (logger == null) {
                logger = new ProgressLogger(identifiers.size(), logProgressInterval);
                loggerThreadId = Thread.currentThread().getId();
                LOG.debug("Created new progress logger for thread {} - {} items, logging interval {} ms",
                        loggerThreadId, identifiers.size(), logProgressInterval);
            }
        }

        for (String identifier : identifiers) {
            try {
                new GetRecordQuery(metadataPrefix, identifier).execute(oaipmhServer);
            } catch (RuntimeException e) {
                LOG.error("Error retrieving record {}", identifier, e);
                errors++;
                // if there are too many errors, just abort
                if (errors > MAX_ERRORS_PER_THREAD) {
                    LOG.error("Terminating GetRecord thread {} because too many errors occurred", Thread.currentThread().getId());
                    break;
                }
            }

            if (Thread.currentThread().getId() == loggerThreadId) {
                counter++;
                logger.logProgress(counter);
            }
        }
        return new ListRecordsResult((System.currentTimeMillis() - start) / 1000F, errors);
    }

}
