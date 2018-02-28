package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.service.exception.OaiPmhException;

/**
 * Interface for classes that return record information
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
public interface RecordProvider {

    /**
     * Returns the record information (in xml) of the record with the supplied id
     * @param id
     * @return String with record information (in xml)
     * @throws OaiPmhException when there is a problem retrieving the information (e.g. IdDoesNotExistException)
     */
    public String getRecord(String id) throws OaiPmhException;

    /**
     * Shut down this record provider
     */
    public void close();
}
