package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoRecordsMatchException extends OaiPmhException {
    public NoRecordsMatchException(String msg) {
        super(msg, ErrorCode.NO_RECORDS_MATCH);
    }
}
