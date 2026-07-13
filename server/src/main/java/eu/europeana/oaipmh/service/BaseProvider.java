package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.config.OaiPmhSettings;
import javax.annotation.Resource;

class BaseProvider {

    @Resource
    OaiPmhSettings settings;

    /**
     * Prepares a record ID by removing the identifier prefix if it exists.
     *
     * @param fullId the full identifier, including the potential prefix
     * @return the record ID with the prefix removed if present, or the original
     *         full identifier if the prefix is not found
     */
    String prepareRecordId(String fullId) {
        if (fullId.startsWith(settings.getIdentifierPrefix())) {
            return fullId.substring(settings.getIdentifierPrefix().length());
        }
        return fullId;
    }

    /**
     * Prepares the full identifier for a record by ensuring the specified record ID
     * starts with a forward slash, if not already present, and prepending the identifier prefix.
     *
     * @param recordId the record ID to be prepared
     * @return the full identifier, which consists of the identifier prefix concatenated
     *         with the record ID (ensuring it starts with a forward slash)
     */
    String prepareFullId(String recordId) {
        if (!recordId.startsWith("/")) {
            recordId = "/" + recordId;
        }
        return settings.getIdentifierPrefix() + recordId;
    }


    /**
     * Extracts the set identifier from the provided set name by removing any portion
     * of the string after the first underscore ('_'). If no underscore is found, the
     * original set name is returned unchanged.
     *
     * @param setName the full set name, potentially containing an underscore ('_')
     *                as a delimiter
     * @return the set identifier, which is the substring of the set name before the
     *         first underscore ('_'), or the original set name if no underscore is found
     */
    String getSetIdentifier(String setName) {
        int index = setName.indexOf('_');
        if (index == -1) {
            return setName;
        }
        return setName.substring(0, index);
    }

}
