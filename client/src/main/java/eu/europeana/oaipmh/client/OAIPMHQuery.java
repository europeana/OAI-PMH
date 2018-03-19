package eu.europeana.oaipmh.client;

public interface OAIPMHQuery {

    String VERB_PARAMETER = "verb=%s";

    String getVerbName();

    void execute(OAIPMHServiceClient oaipmhServer);
}
