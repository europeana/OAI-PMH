package eu.europeana.oaipmh.service.exception;

import eu.europeana.oaipmh.model.OAIError;
import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.service.BaseService;
import eu.europeana.oaipmh.service.OaiPmhRequestFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler that catches all errors and logs the interesting ones
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@ControllerAdvice
@RestController
public class GlobalExceptionHandler extends BaseService {

    @Value("${baseURL}")
    private String baseUrl;

    private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class);
    private static final String BAD_REQUEST = "BAD_REQUEST";


    /**
     * Checks if we should log an error and serializes the error response
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler({BadArgumentException.class, BadResumptionToken.class, BadVerbException.class, CannotDisseminateFormatException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request);
    }

    /**
     * Handles record-not-found (404) exceptions
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler({IdDoesNotExistException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request);
    }

    /**
     * Handle badMethod exceptions
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler({BadMethodException.class})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public String handleBadMethod(OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request);
    }

    /**
     * Handle all other 'internal server' problems
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler(OaiPmhException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOther(OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMissingParams(MissingServletRequestParameterException e, HttpServletRequest request) throws BadArgumentException, SerializationException {
        return handleException(new BadArgumentException("Required parameter \"" + e.getParameterName() + "\" is missing"), request);
    }

    private String handleException(OaiPmhException e, HttpServletRequest request) throws BadArgumentException, SerializationException {
        if (e.doLog()) {
            LOG.error(e.getMessage(), e);
        }
        OAIRequest originalRequest = OaiPmhRequestFactory.createRequest(baseUrl, request.getQueryString(), true);
        OAIError error = new OAIError(e.getErrorCode(), e.getMessage());
        return serialize(error.getResponse(originalRequest));
    }

    /**
     * Checks if we should log an error and serializes the error response
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler(NoRecordsMatchException.class)
    @ResponseStatus(HttpStatus.OK)
    public String handleNoRecordsMatchException(NoRecordsMatchException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public final String handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) throws BadArgumentException, SerializationException
    {
        String details = String.join(" ," ,ex.getConstraintViolations()
                .parallelStream()
                .map(e -> e.getMessage())
                .collect(Collectors.toList()));
        return handleException(new BadArgumentException(details),request);
    }
}
