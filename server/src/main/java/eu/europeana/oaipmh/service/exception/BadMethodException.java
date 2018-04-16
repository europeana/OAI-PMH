package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Error that is thrown when we receive a request that isn't a GET or POST
 * @author Patrick Ehlert
 * Created on 16-04-2018
 */
@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class BadMethodException extends OaiPmhException {

    public BadMethodException(String verb) {
        super(verb, ErrorCode.BAD_METHOD);
    }

    @Override
    public boolean doLog() {
        return false;
    }
}
