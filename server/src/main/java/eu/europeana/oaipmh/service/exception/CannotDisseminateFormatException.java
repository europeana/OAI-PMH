package eu.europeana.oaipmh.service.exception;

public class CannotDisseminateFormatException extends OaiPmhException {

    public CannotDisseminateFormatException(String metadataPrefix) {
        super(metadataPrefix, ErrorCode.CANNOT_DISSEMINATE_FORMAT);
    }
}
