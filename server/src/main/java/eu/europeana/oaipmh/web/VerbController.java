package eu.europeana.oaipmh.web;

import eu.europeana.oaipmh.service.OaiPmhService;
import eu.europeana.oaipmh.service.exception.BadArgumentException;
import eu.europeana.oaipmh.service.exception.BadVerbException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


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

    private static final Set<String> VALID_PARAMETER_NAMES = new HashSet<>();

    static {
        VALID_PARAMETER_NAMES.add("metadataPrefix");
        VALID_PARAMETER_NAMES.add("from");
        VALID_PARAMETER_NAMES.add("until");
        VALID_PARAMETER_NAMES.add("set");
        VALID_PARAMETER_NAMES.add("resumptionToken");
        VALID_PARAMETER_NAMES.add("verb");
        VALID_PARAMETER_NAMES.add("identifier");
    }

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
        validateParameterNames(request.getQueryString());
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
                                  @RequestParam(value = "identifier", required = true) String identifier,
                                  HttpServletRequest request) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return ops.getRecord(metadataPrefix, identifier);
    }

    /**
     * Handles all list identifier requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = {"verb=ListIdentifiers", "resumptionToken"}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListIdentifiersToken(@RequestParam(value = "resumptionToken", required = true) String resumptionToken, HttpServletRequest request) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return ops.listIdentifiers(resumptionToken);
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
    @RequestMapping(params = {"verb=ListIdentifiers", "metadataPrefix"}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListIdentifiers(@RequestParam(value = "metadataPrefix", required = true) String metadataPrefix,
                                        @RequestParam(value = "from", required = false) String from,
                                        @RequestParam(value = "until", required = false) String until,
                                        @RequestParam(value = "set", required = false) String set,
                                        HttpServletRequest request) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        validateParameters(set, from, until);
        return ops.listIdentifiers(metadataPrefix, DateConverter.fromIsoDateTime(from), DateConverter.fromIsoDateTime(until), set);
    }

    /**
     * Validate whether used parameters have valid names
     *
     * @param queryString query string containing all the parameters specified in the request
     * @throws BadArgumentException
     */
    private void validateParameterNames(String queryString) throws BadArgumentException {
        String[] arguments = queryString.split("&");
        for (String argument : arguments) {
            String[] paramValue = argument.split("=");
            if (!VALID_PARAMETER_NAMES.contains(paramValue[0])) {
                throw new BadArgumentException("Parameter name \"" + paramValue[0] + "\" is not supported!");
            }
        }
    }

    /**
     * Validates a request parameter and detects whether it is empty or was specified multiple times.
     * When any of these defects occurs BadArgumentException is thrown.
     *
     * @param name  name of the parameter that is verified
     * @param value paramer value
     * @throws BadArgumentException
     */
    private void validateParameter(String name, String value) throws BadArgumentException {
        // skip when null
        if (value == null) {
            return;
        }
        // empty
        if (value != null && value.isEmpty()) {
            throw new BadArgumentException("Parameter \"" + name + "\" cannot be empty");
        }
        // specified multiple times
        String[] split = value.split(",");
        if (split.length > 1) {
            throw new BadArgumentException("Parameter \"" + name + "\" can be specified only once.");
        }
    }

    /**
     * Validates specified parameters and throws BadArgumentException when any of them is incorrect.
     *
     * @param set set name
     * @param from from date
     * @param until until date
     * @throws OaiPmhException
     */
    private void validateParameters(String set, String from, String until) throws OaiPmhException {
        validateParameter("set", set);
        validateParameter("from", from);
        validateParameter("until", until);

        validateDateParameters(from, until);
    }

    /**
     * Validates from and until dates parameters. Detects whether they have wrong format or from is later than until.
     *
     * @param from  from date
     * @param until until date
     * @throws BadArgumentException
     */
    private void validateDateParameters(String from, String until) throws BadArgumentException {
        try {
            if (from != null || until != null) {
                Date fromDate = DateConverter.fromIsoDateTime(from);
                Date untilDate = DateConverter.fromIsoDateTime(until);
                if (fromDate != null && untilDate != null && fromDate.after(untilDate)) {
                    throw new BadArgumentException("Parameter \"from\" must specify date that is before \"until\".");
                }
            }
        } catch (IllegalArgumentException e) {
            // thrown when any specified date is incorrect
            throw new BadArgumentException("Either \"from\" or \"until\" parameter specifies incorrect date. Proper date format is YYYY-MM-DDThh:mm:ssZ.");
        }
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
                                    @RequestParam(value = "set", required = false) String set,
                                    HttpServletRequest request) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
        return "Not implemented yet";
    }

    /**
     * Handles all list records requests with a resumption token
     * @param resumptionToken
     * @return
     * @throws OaiPmhException
     */
    @RequestMapping(params = {"verb=ListRecords", "resumptionToken!="}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListRecordsToken(@RequestParam(value = "resumptionToken", required = true) String resumptionToken,
                                         HttpServletRequest request) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
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
        validateParameterNames(request.getQueryString());
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
    @RequestMapping(params = {"verb=ListSets", "resumptionToken!="}, method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_XML_VALUE)
    public String handleListSetsToken(@RequestParam(value = "resumptionToken", required = true) String resumptionToken,
                                      HttpServletRequest request) throws OaiPmhException {
        validateParameterNames(request.getQueryString());
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
