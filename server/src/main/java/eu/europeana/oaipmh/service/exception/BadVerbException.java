package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Error that is thrown when a request contains no verb or an unknown verb
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BadVerbException extends OaiPmhException {

    public BadVerbException(String verb) {
        super(verb, ErrorCode.BAD_VERB);
    }

    @Override
    public boolean doLog() {
        return false;
    }
}
