package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadResumptionToken extends OaiPmhException {
    public BadResumptionToken(String msg) {
        super(msg, ErrorCode.BAD_RESUMPTION_TOKEN);
    }
}
