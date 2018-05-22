package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.service.exception.OaiPmhException;

public interface IdentifyProvider {
    Identify provideIdentify() throws OaiPmhException;
}
