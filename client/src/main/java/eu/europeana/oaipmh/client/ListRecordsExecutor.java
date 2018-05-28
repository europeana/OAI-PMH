package eu.europeana.oaipmh.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.Callable;

public class ListRecordsExecutor implements Callable<ListRecordsResult> {

    private static final Logger LOG = LogManager.getLogger(ListRecordsExecutor.class);

    private static final int MAX_ERRORS_PER_THREAD = 50;

    private List<String> identifiers;

    private String metadataPrefix;

    private OAIPMHServiceClient oaipmhServer;

    public ListRecordsExecutor(List<String> identifiers, String metadataPrefix, OAIPMHServiceClient oaipmhServer) {
        this.identifiers = identifiers;
        this.metadataPrefix = metadataPrefix;
        this.oaipmhServer = oaipmhServer;
    }

    @Override
    public ListRecordsResult call() throws Exception {
        int errors = 0;
        long start = System.currentTimeMillis();
        for (String identifier : identifiers) {
            try {
                new GetRecordQuery(metadataPrefix, identifier).execute(oaipmhServer);
            } catch (Exception e) {
                LOG.error("Error retrieving record {}", identifier, e);
                errors++;
                // if there are too many errors, just abort
                if (errors > MAX_ERRORS_PER_THREAD) {
                    break;
                }
            }
        }
        return new ListRecordsResult((System.currentTimeMillis() - start) / 1000, errors);
    }
}
