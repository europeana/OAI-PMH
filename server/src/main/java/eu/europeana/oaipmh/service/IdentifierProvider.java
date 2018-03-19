package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.service.exception.OaiPmhException;

import java.util.Date;

/**
 * Interface for classes that return identifier information
 */
public interface IdentifierProvider extends ClosableProvider {

    /**
     * Returns the list of header objects containing identifier, creation date and list of datasets. It also contains
     * resumption token and information about the complete size (optional).
     * @param metadataPrefix metadata prefix of identifiers
     * @param from starting date
     * @param until ending date
     * @param set set that the identifier belongs to
     * @return object with the list of Header objects containing necessary information
     * @throws OaiPmhException when there is a problem retrieving the information (e.g. IdDoesNotExistException)
     */
    ListIdentifiers listIdentifiers(String metadataPrefix, Date from, Date until, String set) throws OaiPmhException;

    /**
     * Returns next page of list identifiers. ResumptionToken must be a valid string retrieved by calling 4 argument version of listIdentifiers.
     *
     * @param resumptionToken token string used to retrieve next page of results
     * @return object with a list of header objects from the next page
     * @throws OaiPmhException
     */
    ListIdentifiers listIdentifiers(String resumptionToken) throws OaiPmhException;
}
