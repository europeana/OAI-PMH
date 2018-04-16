package eu.europeana.oaipmh.service;

import org.springframework.beans.factory.annotation.Value;

class BaseProvider {
    @Value("${identifierPrefix}")
    private String identifierPrefix;

    String prepareRecordId(String fullId) {
        if (fullId.startsWith(identifierPrefix)) {
            return fullId.substring(identifierPrefix.length());
        }
        return fullId;
    }

    String prepareFullId(String recordId) {
        if (!recordId.startsWith("/")) {
            recordId = "/" + recordId;
        }
        return identifierPrefix + recordId;
    }
}
