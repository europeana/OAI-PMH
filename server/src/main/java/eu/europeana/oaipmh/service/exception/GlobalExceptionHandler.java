package eu.europeana.oaipmh.service.exception;

import eu.europeana.oaipmh.model.OAIError;
import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.service.BaseService;
import eu.europeana.oaipmh.service.OaiPmhRequestFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
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
    private static final String MEDIA_TYPE_TEXT_XML = "text/xml;charset=UTF-8";

    /**
     * Checks if we should log an error and serializes the error response
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler({BadArgumentException.class, BadResumptionToken.class, BadVerbException.class, CannotDisseminateFormatException.class})
    public ResponseEntity<String> handleBadRequest(OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles record-not-found (404) exceptions
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler({IdDoesNotExistException.class})
    public ResponseEntity<String> handleNotFound(OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle badMethod exceptions
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler({BadMethodException.class})
    public ResponseEntity<String> handleBadMethod(OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle all other 'internal server' problems
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler(OaiPmhException.class)
    public ResponseEntity<String> handleOther(OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException e, HttpServletRequest request)
            throws OaiPmhException {
        return handleException(new BadArgumentException("Required parameter \"" + e.getParameterName() + "\" is missing"), request, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<String> handleException(OaiPmhException e, HttpServletRequest request, HttpStatus httpStatus)
            throws OaiPmhException {
        if (e.doLog()) {
            LOG.error(e.getMessage(), e);
        }
        OAIRequest originalRequest = OaiPmhRequestFactory.createRequest(baseUrl, request.getQueryString(), true);
        OAIError error = new OAIError(e.getErrorCode(), e.getMessage());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.valueOf(MEDIA_TYPE_TEXT_XML));
        return new ResponseEntity<>(serialize(error.getResponse(originalRequest)), responseHeaders, httpStatus);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request)
            throws OaiPmhException {
        String details = String.join(" ," ,ex.getConstraintViolations()
                .parallelStream()
                .map(e -> e.getMessage())
                .collect(Collectors.toList()));
        return handleException(new BadArgumentException(details),request, HttpStatus.BAD_REQUEST);
    }

}
