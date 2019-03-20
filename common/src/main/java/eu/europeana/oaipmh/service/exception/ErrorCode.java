package eu.europeana.oaipmh.service.exception;

/**
 * The error codes that are defined in OAI-PMH (see also https://www.openarchives.org/OAI/openarchivesprotocol.html#ErrorConditions)
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
public enum ErrorCode {

    BAD_ARGUMENT("badArgument"),
    BAD_RESUMPTION_TOKEN("badResumptionToken"),
    BAD_VERB("badVerb"),
    CANNOT_DISSEMINATE_FORMAT("cannotDisseminateFormat"),
    ID_DOES_NOT_EXIST("idDoesNotExist"),
    NO_RECORDS_MATCH("noRecordsMatch"),
    NO_METADATA_FORMATS("noMetadataFormats"),
    NO_SET_HIERARCHY("noSetHierarchy"),
    BAD_METHOD("badMethod"),
    INTERNAL_ERROR("internalServerError"),
    SWAGGER_CONFIG_ERROR("swaggerProviderConfigError");

    private final String code;

    private ErrorCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }

    public static ErrorCode fromString(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("No ErrorCode enum found for \"" + code + "\"");
    }
}
