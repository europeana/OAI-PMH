package eu.europeana.oaipmh.client;

import java.util.List;
import java.util.concurrent.Callable;

public class ListRecordsExecutor implements Callable<ListRecordsResult> {
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
                errors++;
            }
        }
        return new ListRecordsResult((System.currentTimeMillis() - start) / 1000, errors);
    }
}
