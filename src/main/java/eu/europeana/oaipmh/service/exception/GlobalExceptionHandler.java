package eu.europeana.oaipmh.service.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler that catches all errors and logs the interesting ones
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class);

    // TODO make sure we return xml errors messages instead of json

    /**
     * Checks if we should log an error and rethrows it
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler(OaiPmhException.class)
    public void handleOaiPmhException(OaiPmhException e) throws OaiPmhException {
        if (e.doLog()) {
            LOG.error(e.getMessage(), e);
        }
        throw e;
    }
}
