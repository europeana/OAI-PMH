package eu.europeana.oaipmh.web;

import eu.europeana.oaipmh.config.OaiPmhSettings;
import eu.europeana.oaipmh.model.response.OAIResponse;
import eu.europeana.oaipmh.service.OaiPmhService;
import eu.europeana.oaipmh.service.exception.BadMethodException;
import eu.europeana.oaipmh.service.exception.BadVerbException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.service.exception.SerializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;

import static eu.europeana.oaipmh.service.OaiPmhRequestFactory.*;
import static eu.europeana.oaipmh.service.exception.ErrorConstants.*;
import static eu.europeana.oaipmh.util.AppConfigConstants.XML_SERIALIZATION_PROVIDER;
import static eu.europeana.oaipmh.web.WebConstants.*;

/**
 * Rest controller that handles incoming OAI-PMH requests 
 * (identify, get record, list identifiers, list metadata formats,
 * list records and list sets)
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@Validated
@RestController
@RequestMapping(value = {"/oai", "/oaicat/OAIHandler"})
public class VerbController {

    private static final String REGEX_VALID_SET_ID = "^[a-zA-Z0-9-_]*$";

    private XmlMapper     serialization;
    private OaiPmhService ops;

    @Resource
    OaiPmhSettings settings;

    @Autowired
    public VerbController(OaiPmhService oaiPmhService,
                          @Qualifier(value = XML_SERIALIZATION_PROVIDER) XmlMapper serialization) {
        this.ops = oaiPmhService;
        this.serialization = serialization;
    }

    /**
     * Handles all identify requests
     * @return
     * @throws OaiPmhException when there's a problem processing request 
     *         parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even 
    //if post doesn't change any state
    @SuppressWarnings({"squid:S3752"
                     , "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = "verb=Identify",
                    produces = { MediaType.APPLICATION_XML_VALUE
                               , MediaType.TEXT_XML_VALUE })
    public ResponseEntity<StreamingResponseBody> handleIdentify(
            HttpServletRequest request, HttpServletResponse response) 
                throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return respond(ops.getIdentify(createIdentifyRequest(settings.getBaseUrl())));
    }

    /**
     * Handles all getRecord requests
     * @param identifier
     * @param metadataPrefix
     * @return
     * @throws OaiPmhException when there's a problem processing request 
     * parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if 
    // post doesn't change any state
    @SuppressWarnings({"squid:S3752"
                     , "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = "verb=GetRecord",
                    produces = { MediaType.APPLICATION_XML_VALUE
                               , MediaType.TEXT_XML_VALUE })
    public ResponseEntity<StreamingResponseBody> handleGetRecord(
            @RequestParam(value = "metadataPrefix", required = true) String metadataPrefix,
            @RequestParam(value = "identifier", required = true) String identifier,
            HttpServletRequest request,
            HttpServletResponse response) 
            throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return respond(ops.getRecord(
            createGetRecordRequest(settings.getBaseUrl(), metadataPrefix, identifier)));
    }

    /**
     * Handles all list identifier requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException when there's a problem processing request 
     * parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if 
    // post doesn't change any state
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = {"verb=ListIdentifiers", "resumptionToken", "!metadataPrefix", "!set", "!from", "!until"},
                    produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<StreamingResponseBody> handleListIdentifiersToken(
            @RequestParam(value = "resumptionToken") String resumptionToken,
            HttpServletRequest request,
            HttpServletResponse response) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return respond(ops.listIdentifiers(
            createListIdentifiersRequest(settings.getBaseUrl(), resumptionToken)));
    }

    /**
     * Handles all list identifier requests
     * @param metadataPrefix
     * @param from
     * @param until
     * @param set
     * @return
     * @throws OaiPmhException when there's a problem processing request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    @SuppressWarnings({ "squid:S3752"
                      , "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING" })
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = {"verb=ListIdentifiers", "metadataPrefix"
                            , "!resumptionToken"},
                    produces = { MediaType.APPLICATION_XML_VALUE
                               , MediaType.TEXT_XML_VALUE })
    public ResponseEntity<StreamingResponseBody> handleListIdentifiers(
            @RequestParam(value = "metadataPrefix") String metadataPrefix,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "until", required = false) String until,
            @RequestParam(value = "set", required = false) 
                @Pattern(regexp = REGEX_VALID_SET_ID,
                         message = INVALID_SET_ID_MSG) String set,
            HttpServletRequest request,
            HttpServletResponse response) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return respond(ops.listIdentifiers(
            createListIdentifiersRequest(settings.getBaseUrl(), metadataPrefix, set
                                       , from, until)));
    }

    /**
     * Handles all list records requests
     * @param metadataPrefix
     * @param from
     * @param until
     * @param set
     * @return
     * @throws OaiPmhException when there's a problem processing request 
     * parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if 
    // post doesn't change any state
    @SuppressWarnings({"squid:S3752"
                     , "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = { "verb=ListRecords", "metadataPrefix"
                             , "!resumptionToken"},
                    produces = { MediaType.APPLICATION_XML_VALUE
                               , MediaType.TEXT_XML_VALUE})
    public ResponseEntity<StreamingResponseBody> handleListRecords(
            @RequestParam(value = "metadataPrefix") String metadataPrefix,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "until", required = false) String until,
            @RequestParam(value = "set", required = false ) 
                @Pattern(regexp = REGEX_VALID_SET_ID,
                         message = INVALID_SET_ID_MSG) String set,
            HttpServletRequest request,
            HttpServletResponse response) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return respond(ops.listRecords(
            createListRecordsRequest(settings.getBaseUrl(), metadataPrefix, set, from, until)));
    }

    /**
     * Handles all list records requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException when there's a problem processing request 
     * parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if 
    // post doesn't change any state
    @SuppressWarnings({"squid:S3752"
                     , "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = { "verb=ListRecords", "resumptionToken"
                             , "!metadataPrefix", "!set", "!from", "!until"},
                    produces = { MediaType.APPLICATION_XML_VALUE
                               , MediaType.TEXT_XML_VALUE})
    public ResponseEntity<StreamingResponseBody> handleListRecordsToken(
            @RequestParam(value = "resumptionToken") String resumptionToken,
            HttpServletRequest request,
            HttpServletResponse response) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return respond(ops.listRecords(
            createListRecordsRequest(settings.getBaseUrl(), resumptionToken)));
    }

    /**
     * Handles all list identifier requests
     * @param identifier
     * @return
     * @throws OaiPmhException when there's a problem processing request 
     * parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if 
    // post doesn't change any state
    @SuppressWarnings({ "squid:S3752"
                      , "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING" })
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = "verb=ListMetadataFormats",
                    produces = { MediaType.APPLICATION_XML_VALUE
                               , MediaType.TEXT_XML_VALUE})
    public ResponseEntity<StreamingResponseBody> handleListMetadataFormats(
            @RequestParam(value = "identifier", required = false) String identifier,
            HttpServletRequest request,
            HttpServletResponse response) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return respond(ops.listMetadataFormats(
            createListMetadataFormatsRequest(settings.getBaseUrl(), identifier)));
    }

    /**
     * Handles all list sets requests
     * @param from
     * @param until
     * @return
     * @throws OaiPmhException when there's a problem processing 
     * request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if 
    // post doesn't change any state
    @SuppressWarnings({ "squid:S3752"
                      , "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING" })
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = "verb=ListSets",
                    produces = { MediaType.APPLICATION_XML_VALUE
                               , MediaType.TEXT_XML_VALUE})
    public ResponseEntity<StreamingResponseBody> handleListSets(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "until", required = false) String until,
            HttpServletRequest request, 
            HttpServletResponse response) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return respond(ops.listSets(createListSetsRequest(settings.getBaseUrl(), from, until)));
    }

    /**
     * Handles all list sets requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException when there's a problem processing 
     * request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if 
    // post doesn't change any state
    @SuppressWarnings({ "squid:S3752"
                      , "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING "})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = {"verb=ListSets", "resumptionToken"},
                    produces = { MediaType.APPLICATION_XML_VALUE
                               , MediaType.TEXT_XML_VALUE})
    public ResponseEntity<StreamingResponseBody> handleListSetsToken(
            @RequestParam(value = "resumptionToken") String resumptionToken,
            HttpServletRequest request,
            HttpServletResponse response) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return respond(ops.listSets(createListSetsRequest(settings.getBaseUrl(), resumptionToken)));
    }

    /**
     * Since the OAI-PMH protocol requires us to return a specific error for 
     * illegal or missing verbs, we catch all requests with other verbs as well
     * Note that we do not check for repeating verbs, spring-boot will act 
     * on the first verb that is found
     * @return
     * @throws OaiPmhException when an unknown verb or unknown parameter is used
     */
    // oai-pmh protocol requires us to support both post and get even if 
    // post doesn't change any state
    @SuppressWarnings({ "squid:S3752"
                      , "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    produces = { MediaType.APPLICATION_XML_VALUE
                               , MediaType.TEXT_XML_VALUE })
    public String handleIllegalVerbs(
            @RequestParam(value = "verb", required = false) String verb,
            HttpServletRequest request,
            HttpServletResponse response) throws OaiPmhException {
        validateVerb(verb);
        validateParameterNames(request.getQueryString());
        throw new BadVerbException(verb);
    }

    /**
     * Fall-back method so we catch all other (xml) verb requests and return 
     * a badMethod exception when no GET or POST is used
     * @param verb
     * @return
     * @throws BadMethodException when no GET or POST is used in a verb request
     */
    // oai-pmh protocol requires us to support both post and get even if 
    // post doesn't change any state also we intentionally want to catch all 
    // other methods here
    @SuppressWarnings({ "squid:S3752"
                      , "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(produces = { MediaType.APPLICATION_XML_VALUE
                              ,  MediaType.TEXT_XML_VALUE })
    public String handleIllegalMethods(
            @RequestParam(value = "verb", required = false) String verb,
            HttpServletRequest request,
            HttpServletResponse response) throws BadMethodException {
        throw new BadMethodException(msg(BAD_METHOD_MSG, request.getMethod()));
    }

    private ResponseEntity<StreamingResponseBody> respond(OAIResponse rsp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MEDIA_TYPE_TEXT_XML));

        return new ResponseEntity<StreamingResponseBody>(
            new StreamingResponseBody() {
                @Override
                public void writeTo(OutputStream out) throws IOException {
                    try (rsp) {
                        serialization.writeValue(out, rsp);
                    }
                    catch (Exception e) {
                        throw new SerializationException(
                                msg(SERIALIZATION_MSG, e.getMessage()), e);
                    }
                }
            }, headers, HttpStatus.OK);
    }
}
