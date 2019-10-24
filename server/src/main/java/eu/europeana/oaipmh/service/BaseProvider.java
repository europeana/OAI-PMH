package eu.europeana.oaipmh.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

class BaseProvider {
    @Value("${identifierPrefix}")
    private String identifierPrefix;

    private static final Logger LOG = LogManager.getLogger(OaiPmhService.class);

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
     * Retrieve set identifier from the set name. Set name always starts with identifier concatenated with the rest of the name using "_"
     *
     * @param setName name of the set
     * @return set identifier
     */
    String getSetIdentifier(String setName) {
        int index = setName.indexOf('_');
        if (index == -1) {
            return setName;
        }
        if(ifSetNameEndsWithLetter(setName.charAt(index-1))){
            LOG.info("Set name with alphabet {}" , setName);
            return setName.substring(0, index-1);
        }
        return setName.substring(0, index);
    }

    boolean ifSetNameEndsWithLetter(char c){
        if(Character.isLetter(c)) {
            return true;
        }
        return false;
    }
}
