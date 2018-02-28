package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Error that is thrown when there is an error during serialization
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SerializationException extends OaiPmhException {

    public SerializationException(String msg, Throwable t) {
        super(msg, t);
    }
}
