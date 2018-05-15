package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception used when noRecordsMatch error should be returned.
 */
@ResponseStatus(HttpStatus.OK)
public class NoRecordsMatchException extends OaiPmhException {

    public NoRecordsMatchException(String msg) {
        super(msg, ErrorCode.NO_RECORDS_MATCH);
    }

    @Override
    public boolean doLog() {
        return false;
    }
}
