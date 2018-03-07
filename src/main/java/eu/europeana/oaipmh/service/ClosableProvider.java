package eu.europeana.oaipmh.service;

public interface ClosableProvider {
    /**
     * Shut down this provider
     */
    void close();
}
