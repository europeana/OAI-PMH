package eu.europeana.oaipmh.service.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception used when cannotDisseminateFormat error should be returned.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CannotDisseminateFormatException extends OaiPmhException {

    public CannotDisseminateFormatException(String metadataPrefix) {
        super(metadataPrefix, ErrorCode.CANNOT_DISSEMINATE_FORMAT);
    }
}
