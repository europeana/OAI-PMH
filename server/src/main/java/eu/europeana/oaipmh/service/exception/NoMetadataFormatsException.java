package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception used when noMetadataFormats error should be returned.
 */
@ResponseStatus(HttpStatus.OK)
public class NoMetadataFormatsException extends OaiPmhException {

    public NoMetadataFormatsException(String msg) {
        super(msg, ErrorCode.NO_METADATA_FORMATS);
    }

    @Override
    public boolean doLog() {
        return false;
    }
}
