package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends OaiPmhException {
    public InternalServerErrorException(String message) {
        super(message, ErrorCode.INTERNAL_ERROR);
    }

    @Override
    public boolean doLog() {
        return true;
    }
}
