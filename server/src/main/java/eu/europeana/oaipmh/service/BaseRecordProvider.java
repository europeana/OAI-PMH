package eu.europeana.oaipmh.service;

import org.springframework.beans.factory.annotation.Value;

class BaseRecordProvider {
    @Value("${identifierPrefix}")
    private String identifierPrefix;

    String prepareId(String id) {
        if (id.startsWith(identifierPrefix)) {
            return id.substring(identifierPrefix.length());
        }
        return id;
    }
}
