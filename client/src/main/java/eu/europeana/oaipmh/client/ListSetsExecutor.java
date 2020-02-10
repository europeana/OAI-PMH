package eu.europeana.oaipmh.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.Callable;

public class ListSetsExecutor implements Callable<ListRecordsResult> {

    private static final Logger LOG = LogManager.getLogger(ListSetsExecutor.class);

    private static final int MAX_ERRORS_PER_THREAD = 2;

    private static ProgressLogger logger = null;
    private static long loggerThreadId;
    private int logProgressInterval;

    private List<String> sets;

    private String directoryLocation;

    private String saveToFile;

    private String metadataPrefix;


    private OAIPMHServiceClient oaipmhServer;

    public ListSetsExecutor(List<String> sets, String metadataPrefix, String directoryLocation, String saveToFile, OAIPMHServiceClient oaipmhServer, int logProgressInterval) {
        this.sets = sets;
        this.metadataPrefix = metadataPrefix;
        this.directoryLocation = directoryLocation;
        this.saveToFile = saveToFile;
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
                logger = new ProgressLogger(sets.size(), logProgressInterval);
                loggerThreadId = Thread.currentThread().getId();
                LOG.debug("Created new progress logger for thread {} - {} items, logging interval {} ms",
                        loggerThreadId, sets.size(), logProgressInterval);
            }
        }
        for(String set : sets ) {
            try {
                new ListRecordsQuery(metadataPrefix, set, directoryLocation, saveToFile, logProgressInterval).execute(oaipmhServer);
            } catch (Exception e) {
                LOG.error("Error retrieving set {} {}", set,  e);
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
