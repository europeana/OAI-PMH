package eu.europeana.oaipmh.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception used when there's a problem putting the fake Swagger api-docs.json file together
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SwaggerProviderException extends OaiPmhException {

    public SwaggerProviderException(String argument) {
        super(argument, ErrorCode.SWAGGER_CONFIG_ERROR);
    }

    @Override
    public boolean doLog() {
        return false;
    }

}
