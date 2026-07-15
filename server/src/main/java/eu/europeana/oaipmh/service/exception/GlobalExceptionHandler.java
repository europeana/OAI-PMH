package eu.europeana.oaipmh.service.exception;

import eu.europeana.oaipmh.config.OaiPmhSettings;
import eu.europeana.oaipmh.model.OAIError;
import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.OAIResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.annotation.Resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Collectors;

import static eu.europeana.oaipmh.service.OaiPmhRequestFactory.*;
import static eu.europeana.oaipmh.service.exception.ErrorConstants.*;
import static eu.europeana.oaipmh.util.AppConfigConstants.XML_DEFAULT_SERIALIZATION;
import static eu.europeana.oaipmh.web.WebConstants.*;

/**
 * Global exception handler that catches all errors and logs the interesting ones
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class);

    @Resource
    OaiPmhSettings settings;

    @Resource(name = XML_DEFAULT_SERIALIZATION)
    XmlMapper serialization;

//    private static final XmlMapper serialization
//        = new DefaultSerializationProvider().getSerialization();

    /**
     * Checks if we should log an error and serializes the error response
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler({ BadArgumentException.class
                      , BadResumptionToken.class
                      , BadVerbException.class
                      , CannotDisseminateFormatException.class})
    public ResponseEntity<StreamingResponseBody> handleBadRequest(
            OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles record-not-found (404) exceptions
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler({IdDoesNotExistException.class})
    public ResponseEntity<StreamingResponseBody> handleNotFound(
            OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle badMethod exceptions
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler({BadMethodException.class})
    public ResponseEntity<StreamingResponseBody> handleBadMethod(
            OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle all other 'internal server' problems
     * @param e
     * @throws OaiPmhException
     */
    @ExceptionHandler(OaiPmhException.class)
    public ResponseEntity<StreamingResponseBody> handleOther(
            OaiPmhException e, HttpServletRequest request) throws OaiPmhException {
        return handleException(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<StreamingResponseBody> handleMissingParams(
            MissingServletRequestParameterException e, HttpServletRequest request)
            throws OaiPmhException {
        return handleException(
            new BadArgumentException(msg(BAD_ARGUMENT_MSG, e.getParameterName()))
          , request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<StreamingResponseBody> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request)
            throws OaiPmhException {
        String details = String.join(" ," ,ex.getConstraintViolations()
                .parallelStream()
                .map(e -> e.getMessage())
                .collect(Collectors.toList()));
        return handleException(
            new BadArgumentException(details)
          , request, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<StreamingResponseBody> handleException(
            OaiPmhException e, HttpServletRequest request, HttpStatus status)
            throws OaiPmhException {
        if (e.doLog()) {
            LOG.error(e.getMessage(), e);
        }
        OAIRequest origRequest = createRequest(settings.getBaseUrl()
                                             , request.getQueryString(), true);
        OAIError error = new OAIError(e.getErrorCode(), e.getMessage());
        return respond(new OAIResponse(origRequest, error), status);
    }

    // TODO serialization is working , but StreamResponseBody is never invoked
    private ResponseEntity<StreamingResponseBody> respond(
            OAIResponse rsp, HttpStatus status) {
        write(System.out, rsp); // todo remove it later (serialization is working)

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MEDIA_TYPE_TEXT_XML));

        StreamingResponseBody body = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                write(out, rsp);
            }
        };
        System.out.println("body = " + body);
        return new ResponseEntity<>(body, headers, status);
    }

    private void write(OutputStream out, OAIResponse rsp ) {
        try {
            serialization.writeValue(out, rsp);
        }
        catch (IOException e) {
            throw new SerializationException(
                    msg(SERIALIZATION_MSG, e.getMessage()), e);
        }
    }
}
