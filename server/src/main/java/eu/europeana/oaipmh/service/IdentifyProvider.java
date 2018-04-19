package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Identify;

public interface IdentifyProvider {
    Identify provideIdentify();
}
