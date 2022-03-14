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

    /**
     * Retrieve set identifier from the set name. Set name starts with identifier concatenated with the rest of the name.
     *
     * @param datasetName name of the set
     * @return set identifier
     */
    public String datasetNameToId(String datasetName) {
        return datasetName.replaceAll("(\\d+).+", "$1");
    }

}
