package eu.europeana.oaipmh.client;

public class ListRecordsResult {
    private int errors;

    // execution time
    private float time;

    ListRecordsResult(float time, int errors) {
        this.time = time;
        this.errors = errors;
    }

    int getErrors() {
        return errors;
    }

    float getTime() {
        return time;
    }
}
