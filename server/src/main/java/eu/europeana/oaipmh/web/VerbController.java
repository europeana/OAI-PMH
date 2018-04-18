package eu.europeana.oaipmh.web;

import eu.europeana.oaipmh.service.OaiPmhRequestFactory;
import eu.europeana.oaipmh.service.OaiPmhService;
import eu.europeana.oaipmh.service.exception.BadMethodException;
import eu.europeana.oaipmh.service.exception.BadVerbException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * Rest controller that handles incoming OAI-PMH requests (identify, get record, list identifiers, list metadata formats,
 * list records and list sets)
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@RestController
@RequestMapping(value = "/oai")
public class VerbController {

    private static final Logger LOG = LogManager.getLogger(VerbController.class);

    @Value("${baseUrl}")
    private String baseUrl;

    private OaiPmhService ops;

    public VerbController(OaiPmhService oaiPmhService) {
        this.ops = oaiPmhService;
    }

    /**
     * Handles all identify requests
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = "verb=Identify", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public Object handleIdentify(HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.getIdentify(OaiPmhRequestFactory.createIdentifyRequest(baseUrl));
    }

    /**
     * Handles all getRecord requests
     * @param identifier
     * @param metadataPrefix
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = "verb=GetRecord", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
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
     * @throws OaiPmhException
     */
    @RequestMapping(params = {"verb=ListIdentifiers", "resumptionToken", "!metadataPrefix", "!set", "!from", "!until"}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListIdentifiersToken(@RequestParam(value = "resumptionToken") String resumptionToken, HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return ops.listIdentifiersWithToken(OaiPmhRequestFactory.createListIdentifiersRequest(baseUrl, resumptionToken));
    }

    /**
     * Handles all list identifier requests
     * @param metadataPrefix
     * @param from
     * @param until
     * @param set
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = {"verb=ListIdentifiers", "metadataPrefix", "!resumptionToken"}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListIdentifiers(@RequestParam(value = "metadataPrefix") String metadataPrefix,
                                        @RequestParam(value = "from", required = false) String from,
                                        @RequestParam(value = "until", required = false) String until,
                                        @RequestParam(value = "set", required = false) String set,
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
     * @throws OaiPmhException
     */
    @RequestMapping(params = {"verb=ListRecords", "metadataPrefix", "!resumptionToken"}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListRecords(@RequestParam(value = "metadataPrefix") String metadataPrefix,
                                    @RequestParam(value = "from", required = false) String from,
                                    @RequestParam(value = "until", required = false) String until,
                                    @RequestParam(value = "set", required = false) String set,
                                    HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return "Not implemented yet";
    }

    /**
     * Handles all list records requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = {"verb=ListRecords", "resumptionToken", "!metadataPrefix"}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListRecordsToken(@RequestParam(value = "resumptionToken") String resumptionToken,
                                         HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return "Not implemented yet";
    }

    /**
     * Handles all list identifier requests
     * @param identifier
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = "verb=ListMetadataFormats", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListMetadataFormats(@RequestParam(value = "identifier", required = false) String identifier,
                                            HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return "Not implemented yet";
    }

    /**
     * Handles all list sets requests
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = "verb=ListSets", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListSets() throws OaiPmhException {
        return "Not implemented yet";
    }

    /**
     * Handles all list sets requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = {"verb=ListSets", "resumptionToken"}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListSetsToken(@RequestParam(value = "resumptionToken") String resumptionToken,
                                      HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        return "Not implemented yet";
    }

    /**
     * Since the OAI-PMH protocol requires us to return a specific error for illegal or missing verbs, we catch all requests with other verbs as well
     * Note that we do not check for repeating verbs, spring-boot will act on the first verb that is found
     * @return IllegalVerbException
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleIllegalVerbs(@RequestParam(value = "verb", required = false) String verb, HttpServletRequest request) throws OaiPmhException {
        OaiPmhRequestFactory.validateVerb(verb);
        OaiPmhRequestFactory.validateParameterNames(request.getQueryString());
        throw new BadVerbException(verb);
    }

    /**
     * Return a badMethod exception when no GET or POST is used
     * @param verb
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(produces = MediaType.TEXT_XML_VALUE)
    public String handleIllegalMethods(@RequestParam(value = "verb", required = false) String verb, HttpServletRequest request) throws OaiPmhException {
        throw new BadMethodException(request.getMethod() + " is not allowed.");
    }
}
