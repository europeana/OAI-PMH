package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.OaiPmhException;

import java.util.List;

/**
 * Interface for classes that return record information
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
public interface RecordProvider extends ClosableProvider {

    /**
     * Returns the record information (in xml) of the record with the supplied id
     * @param id identifier of the records to retrieve
     * @return String with record information (in xml)
     * @throws OaiPmhException when there is a problem retrieving the information (e.g. IdDoesNotExistException)
     */
    Record getRecord(String id) throws OaiPmhException;

    /**
     * Returns the ListRecords object containing the records of the specified identifiers.
     *
     * @param identifiers list of headers with identifiers
     * @return ListRecords object with metadata of records for specified identifiers
     * @throws OaiPmhException
     */
    ListRecords listRecords(List<Header> identifiers) throws OaiPmhException;
}
