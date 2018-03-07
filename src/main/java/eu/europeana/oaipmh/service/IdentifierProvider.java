package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.service.exception.OaiPmhException;

import java.util.Date;
import java.util.List;

/**
 * Interface for classes that return identifier information
 */
public interface IdentifierProvider extends ClosableProvider {

    /**
     * Returns the record information (in xml) of the record with the supplied id
     * @param metadataPrefix metadata prefix of identifiers
     * @param from starting date
     * @param until ending date
     * @param set set that the identifier belongs to
     * @return list of Header objects containg necessary information
     * @throws OaiPmhException when there is a problem retrieving the information (e.g. IdDoesNotExistException)
     */
    List<Header> listIdentifiers(String metadataPrefix, Date from, Date until, String set) throws OaiPmhException;

}
