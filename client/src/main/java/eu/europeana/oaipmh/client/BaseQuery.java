package eu.europeana.oaipmh.client;

import static eu.europeana.oaipmh.client.OAIPMHQuery.VERB_PARAMETER;

public class BaseQuery {

    protected static final String METADATA_PREFIX_PARAMETER = "&metadataPrefix=%s";

    protected static final String FROM_PARAMETER = "&from=%s";

    protected static final String UNTIL_PARAMETER = "&until=%s";

    protected static final String SET_PARAMETER = "&set=%s";

    protected static final String RESUMPTION_TOKEN_PARAMETER = "&resumptionToken=%s";

    protected static final String IDENTIFIER_PARAMETER = "&identifier=%s";

    String getBaseRequest(String oaipmhServer, String verbName) {
        return oaipmhServer +
                "?" +
                String.format(VERB_PARAMETER, verbName);
    }
}
