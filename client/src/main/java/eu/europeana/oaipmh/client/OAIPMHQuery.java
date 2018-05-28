package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.service.exception.OaiPmhException;

public interface OAIPMHQuery {

    String VERB_PARAMETER = "verb=%s";

    String getVerbName();

    void execute(OAIPMHServiceClient oaipmhServer) throws OaiPmhException;
}
