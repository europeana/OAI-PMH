package eu.europeana.oaipmh.web;

import eu.europeana.oaipmh.service.OaiPmhRequestFactory;
import eu.europeana.oaipmh.service.OaiPmhService;
import eu.europeana.oaipmh.service.exception.BadMethodException;
import eu.europeana.oaipmh.service.exception.BadVerbException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.SwaggerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;

/**
 * Rest controller that handles incoming OAI-PMH requests (identify, get record, list identifiers, list metadata formats,
 * list records and list sets)
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@Validated
@RestController
@RequestMapping(value = {"/oai", "/oaicat/OAIHandler"})
public class VerbController {

    private static final String REGEX_VALID_SET_ID = "^[a-zA-Z0-9-_]*$";
    private static final String INVALID_SET_ID_MESSAGE = "Set id is invalid";

    @Value("${baseURL}")
    private String baseUrl;

    private OaiPmhService       ops;
    private SwaggerProvider     swaggerProvider;

    public VerbController(OaiPmhService oaiPmhService, SwaggerProvider swaggerProvider) {
        this.ops = oaiPmhService;
        this.swaggerProvider = swaggerProvider;
    }

    /**
     * Handles all identify requests
     * @return
     * @throws OaiPmhException when there's a problem processing request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = "verb=Identify",
                    produces = MediaType.TEXT_XML_VALUE)
    public Object handleIdentify(HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.getIdentify(OaiPmhRequestFactory.createIdentifyRequest(baseUrl));
    }

    /**
     * Handles all getRecord requests
     * @param identifier
     * @param metadataPrefix
     * @return
     * @throws OaiPmhException when there's a problem processing request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = "verb=GetRecord",
                    produces = MediaType.TEXT_XML_VALUE)
    public String handleGetRecord(@RequestParam(value = "metadataPrefix", required = true) String metadataPrefix,
                                  @RequestParam(value = "identifier", required = true) String identifier,
                                  HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.getRecord(OaiPmhRequestFactory.createGetRecordRequest(baseUrl, metadataPrefix, identifier));
    }

    /**
     * Handles all list identifier requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException when there's a problem processing request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = {"verb=ListIdentifiers", "resumptionToken", "!metadataPrefix", "!set", "!from", "!until"},
                    produces = MediaType.TEXT_XML_VALUE)
    public String handleListIdentifiersToken(@RequestParam(value = "resumptionToken") String resumptionToken,
                                             HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.listIdentifiers(OaiPmhRequestFactory.createListIdentifiersRequest(baseUrl, resumptionToken));
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
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = {"verb=ListIdentifiers", "metadataPrefix", "!resumptionToken"},
                    produces = MediaType.TEXT_XML_VALUE)
    public String handleListIdentifiers(@RequestParam(value = "metadataPrefix") String metadataPrefix,
                                        @RequestParam(value = "from", required = false) String from,
                                        @RequestParam(value = "until", required = false) String until,
                                        @RequestParam(value = "set", required = false) @Pattern(regexp = REGEX_VALID_SET_ID,
                                                message = INVALID_SET_ID_MESSAGE) String set,
                                        HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.listIdentifiers(OaiPmhRequestFactory.createListIdentifiersRequest(baseUrl, metadataPrefix, set, from, until));
    }

    /**
     * Handles all list records requests
     * @param metadataPrefix
     * @param from
     * @param until
     * @param set
     * @return
     * @throws OaiPmhException when there's a problem processing request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = {"verb=ListRecords", "metadataPrefix", "!resumptionToken"},
                    produces = MediaType.TEXT_XML_VALUE)
    public String handleListRecords(@RequestParam(value = "metadataPrefix") String metadataPrefix,
                                    @RequestParam(value = "from", required = false) String from,
                                    @RequestParam(value = "until", required = false) String until,
                                    @RequestParam(value = "set", required = false ) @Pattern(regexp = REGEX_VALID_SET_ID,
                                            message = INVALID_SET_ID_MESSAGE) String set,
                                    HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.listRecords(OaiPmhRequestFactory.createListRecordsRequest(baseUrl, metadataPrefix, set, from, until));
    }

    /**
     * Handles all list records requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException when there's a problem processing request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = {"verb=ListRecords", "resumptionToken", "!metadataPrefix", "!set", "!from", "!until"},
                    produces = MediaType.TEXT_XML_VALUE)
    public String handleListRecordsToken(@RequestParam(value = "resumptionToken") String resumptionToken,
                                         HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.listRecords(OaiPmhRequestFactory.createListRecordsRequest(baseUrl, resumptionToken));
    }

    /**
     * Handles all list identifier requests
     * @param identifier
     * @return
     * @throws OaiPmhException when there's a problem processing request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = "verb=ListMetadataFormats",
                    produces = MediaType.TEXT_XML_VALUE)
    public String handleListMetadataFormats(@RequestParam(value = "identifier", required = false) String identifier,
                                            HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.listMetadataFormats(OaiPmhRequestFactory.createListMetadataFormatsRequest(baseUrl, identifier));
    }

    /**
     * Handles all list sets requests
     * @return
     * @throws OaiPmhException when there's a problem processing request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = "verb=ListSets",
                    produces = MediaType.TEXT_XML_VALUE)
    public String handleListSets(HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.listSets(OaiPmhRequestFactory.createListSetsRequest(baseUrl, null));
    }

    /**
     * Handles all list sets requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException when there's a problem processing request parameters, retrieving data or serializing the response
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    params = {"verb=ListSets", "resumptionToken"},
                    produces = MediaType.TEXT_XML_VALUE)
    public String handleListSetsToken(@RequestParam(value = "resumptionToken") String resumptionToken,
                                      HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.listSets(OaiPmhRequestFactory.createListSetsRequest(baseUrl, resumptionToken));
    }

    /**
     * Since the OAI-PMH protocol requires us to return a specific error for illegal or missing verbs, we catch all requests with other verbs as well
     * Note that we do not check for repeating verbs, spring-boot will act on the first verb that is found
     * @return
     * @throws OaiPmhException when an unknown verb or unknown parameter is used
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST},
                    produces = MediaType.TEXT_XML_VALUE)
    public String handleIllegalVerbs(@RequestParam(value = "verb", required = false) String verb,
                                     HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateVerb(verb);
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        throw new BadVerbException(verb);
    }

    /**
     * Fall-back method so we catch all other (xml) verb requests and return a badMethod exception when no GET or POST
     * is used
     * @param verb
     * @return
     * @throws BadMethodException when no GET or POST is used in a verb request
     */
    // oai-pmh protocol requires us to support both post and get even if post doesn't change any state
    // also we intentionally want to catch all other methods here
    @SuppressWarnings({"squid:S3752", "findsecbugs:SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING"})
    @RequestMapping(produces = MediaType.TEXT_XML_VALUE)
    public String handleIllegalMethods(@RequestParam(value = "verb", required = false) String verb,
                                       HttpServletRequest request) throws BadMethodException {
        throw new BadMethodException(request.getMethod() + " is not allowed.");
    }

    /**
     * Returns a hard-coded json swagger configuration file to work around the problem of Swagger not distinguishing
     * between these OAI-PMH "verbs"
     */
    @GetMapping(value = "/api-docs", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String swaggerDocs() {
        return swaggerProvider.getApiDocs();
    }
}
