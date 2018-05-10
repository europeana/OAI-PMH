package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.service.exception.OaiPmhException;

public interface SetsProvider {
    /**
     * Returns the list of sets containing set identifier, set name and optionally description.
     * @return object with the list of Header objects containing necessary information
     * @throws OaiPmhException when there is a problem retrieving the information (e.g. IdDoesNotExistException)
     */
    ListSets listSets() throws OaiPmhException;

    /**
     * Returns next page of list sets. ResumptionToken is an object which is decoded from a valid string retrieved by calling no argument version of listSets.
     *
     * @param resumptionToken decoded resumption token used to retrieve next page of results
     * @return object with a list of set objects from the next page
     * @throws OaiPmhException
     */
    ListSets listSets(ResumptionToken resumptionToken) throws OaiPmhException;
}
