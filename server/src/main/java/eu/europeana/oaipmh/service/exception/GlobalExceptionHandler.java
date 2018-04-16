package eu.europeana.oaipmh.service.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.OAIError;
import eu.europeana.oaipmh.model.request.ListIdentifiersRequest;
import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.service.BaseService;
import eu.europeana.oaipmh.service.OaiPmhRequestFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Global exception handler that catches all errors and logs the interesting ones
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@ControllerAdvice
@RestController
public class GlobalExceptionHandler extends BaseService {

    @Value("${baseUrl}")
    private String baseUrl;

    private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * Checks if we should log an error and serializes the error response
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler(OaiPmhException.class)
    public String handleOaiPmhException(OaiPmhException e, HttpServletRequest request) throws OaiPmhException, JsonProcessingException {
        if (e.doLog()) {
            LOG.error(e.getMessage(), e);
        }
        OAIRequest originalRequest = OaiPmhRequestFactory.createRequest(baseUrl, request.getQueryString(), true);
        OAIError error = new OAIError(e.getErrorCode(), e.getMessage());
        return getXmlMapper().writerWithDefaultPrettyPrinter().
                writeValueAsString(error.getResponse(originalRequest));
    }

    /**
     * Checks if we should log an error and serializes the error response
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler(NoRecordsMatchException.class)
    @ResponseStatus(HttpStatus.OK)
    public String handleNoRecordsMatchException(NoRecordsMatchException e, HttpServletRequest request) throws OaiPmhException, JsonProcessingException {
        if (e.doLog()) {
            LOG.error(e.getMessage(), e);
        }
        OAIRequest originalRequest = OaiPmhRequestFactory.createRequest(baseUrl, request.getQueryString(), true);
        OAIError error = new OAIError(e.getErrorCode(), e.getMessage());
        return getXmlMapper().writerWithDefaultPrettyPrinter().
                writeValueAsString(error.getResponse(originalRequest));
    }
}
