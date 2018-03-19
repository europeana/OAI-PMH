package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Error that is thrown when the id of a record is missing or no record with a specified id exists
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class IdDoesNotExistException extends OaiPmhException {

    public IdDoesNotExistException(String id) {
        super(id, ErrorCode.ID_DOES_NOT_EXIST);
    }

    @Override
    public boolean doLog() {
        return false;
    }
}
