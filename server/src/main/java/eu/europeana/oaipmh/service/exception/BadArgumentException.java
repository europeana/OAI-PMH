package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadArgumentException extends OaiPmhException {
    public BadArgumentException(String argument) {
        super(argument, ErrorCode.BAD_ARGUMENT);
    }
}
