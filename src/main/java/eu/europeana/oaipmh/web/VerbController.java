package eu.europeana.oaipmh.web;

import eu.europeana.oaipmh.service.OaiPmhService;
import eu.europeana.oaipmh.service.exception.BadVerbException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
    public Object handleIdentify() throws OaiPmhException {
        return ops.getIdentify();
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
                                  @RequestParam(value = "identifier", required = true) String identifier) throws OaiPmhException {
        return ops.getRecord(metadataPrefix, identifier);
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
    @RequestMapping(params = "verb=ListIdentifiers", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListIdentifier(@RequestParam(value = "metadataPrefix", required = true) String metadataPrefix,
                                  @RequestParam(value = "from", required = false) String from,
                                  @RequestParam(value = "until", required = false) String until,
                                  @RequestParam(value = "set", required = false) String set) throws OaiPmhException {
        return "Not implemented yet";
    }

    /**
     * Handles all list identifier requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = {"verb=ListIdentifiers", "resumptionToken!="}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListIdentifierToken(@RequestParam(value = "resumptionToken", required = true) String resumptionToken) throws OaiPmhException {
        LOG.debug("ListIdentifier with token {}", resumptionToken);
        return "Not implemented yet";
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
    @RequestMapping(params = "verb=ListRecords", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListRecords(@RequestParam(value = "metadataPrefix", required = true) String metadataPrefix,
                                    @RequestParam(value = "from", required = false) String from,
                                    @RequestParam(value = "until", required = false) String until,
                                    @RequestParam(value = "set", required = false) String set) throws OaiPmhException {
        LOG.debug("ListRecords with metadataPrefix {}, from {}, until {}, set {}", metadataPrefix, from, until, set);
        return "Not implemented yet";
    }

    /**
     * Handles all list records requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = {"verb=ListRecords", "resumptionToken!="}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListRecordsToken(@RequestParam(value = "resumptionToken", required = true) String resumptionToken) throws OaiPmhException {
        LOG.debug("ListRecords with resumptionToken {}", resumptionToken);
        return "Not implemented yet";
    }

    /**
     * Handles all list identifier requests
     * @param identifier
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = "verb=ListMetadataFormats", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListMetadataFormats(@RequestParam(value = "identifier", required = false) String identifier) throws OaiPmhException {
        LOG.debug("ListMetadataFormat with identifier {}", identifier);
        return "Not implemented yet";
    }

    /**
     * Handles all list sets requests
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = "verb=ListSets", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListSets() throws OaiPmhException {
        LOG.debug("ListSets");
        return "Not implemented yet";
    }

    /**
     * Handles all list sets requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = {"verb=ListSets", "resumptionToken!="}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListSetsToken(@RequestParam(value = "resumptionToken", required = true) String resumptionToken) throws OaiPmhException {
        LOG.debug("ListSets with resumptionToken {}", resumptionToken);
        return "Not implemented yet";
    }

    /**
     * Since the OAI-PMH protocol requires us to return a specific error for illegal or missing verbs, we catch all requests with other verbs as well
     * Note that we do not check for repeating verbs, spring-boot will act on the first verb that is found
     * @return IllegalVerbException
     */
    @RequestMapping()
    public String handleIllegalVerbs(@RequestParam(value = "verb", required = false) String verb) throws OaiPmhException {
        throw new BadVerbException(verb);
    }

}
