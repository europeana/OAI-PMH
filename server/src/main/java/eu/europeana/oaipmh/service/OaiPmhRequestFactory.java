package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.request.*;
import eu.europeana.oaipmh.service.exception.BadArgumentException;
import eu.europeana.oaipmh.service.exception.BadVerbException;
import eu.europeana.oaipmh.util.DateConverter;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OaiPmhRequestFactory {
    private static final String PARAMETER_INFO = "Parameter \"%s\" ";

    private static final Set<String> validVerbs;

    private static final Map<String, Set<OaiParameterName>> validVerbParameters;

    private static final Map<String, Set<OaiParameterName>> mandatoryVerbParameters;

    private static final Map<OaiParameterName, Set<OaiParameterName>> exclusiveParameters;

    static {
        validVerbs = new HashSet<>();
        validVerbs.add(Identify.class.getSimpleName());
        validVerbs.add(ListIdentifiers.class.getSimpleName());
        validVerbs.add(GetRecord.class.getSimpleName());
        validVerbs.add(ListSets.class.getSimpleName());
        validVerbs.add(ListMetadataFormats.class.getSimpleName());

        mandatoryVerbParameters = new HashMap<>();
        // ListIdentifiers
        Set<OaiParameterName> mandatoryParameters = new HashSet<>();
        mandatoryParameters.add(OaiParameterName.METADATA_PREFIX);
        mandatoryVerbParameters.put(ListIdentifiers.class.getSimpleName(), mandatoryParameters);
        // GetRecord
        mandatoryParameters = new HashSet<>();
        mandatoryParameters.add(OaiParameterName.METADATA_PREFIX);
        mandatoryParameters.add(OaiParameterName.IDENTIFIER);
        mandatoryVerbParameters.put(GetRecord.class.getSimpleName(), mandatoryParameters);

        validVerbParameters = new HashMap<>();
        // Identify
        Set<OaiParameterName> validParameters = new HashSet<>();
        validVerbParameters.put(Identify.class.getSimpleName(), validParameters);
        // ListIdentifiers
        validParameters = new HashSet<>();
        validParameters.add(OaiParameterName.METADATA_PREFIX);
        validParameters.add(OaiParameterName.FROM);
        validParameters.add(OaiParameterName.UNTIL);
        validParameters.add(OaiParameterName.SET);
        validParameters.add(OaiParameterName.RESUMPTION_TOKEN);
        validVerbParameters.put(ListIdentifiers.class.getSimpleName(), validParameters);
        // GetRecord
        validParameters = new HashSet<>();
        validParameters.add(OaiParameterName.METADATA_PREFIX);
        validParameters.add(OaiParameterName.IDENTIFIER);
        validVerbParameters.put(GetRecord.class.getSimpleName(), validParameters);
        // ListSets
        validParameters = new HashSet<>();
        validParameters.add(OaiParameterName.RESUMPTION_TOKEN);
        validVerbParameters.put(ListSets.class.getSimpleName(), validParameters);
        // ListMetadataFormats
        validParameters = new HashSet<>();
        validParameters.add(OaiParameterName.IDENTIFIER);
        validVerbParameters.put(ListMetadataFormats.class.getSimpleName(), validParameters);

        exclusiveParameters = new HashMap<>();
        // metadataPrefix
        Set<OaiParameterName> exclusiveForParam = new HashSet<>();
        exclusiveForParam.add(OaiParameterName.RESUMPTION_TOKEN);
        exclusiveParameters.put(OaiParameterName.METADATA_PREFIX, exclusiveForParam);
        // resumptionToken
        exclusiveForParam = new HashSet<>();
        exclusiveForParam.add(OaiParameterName.METADATA_PREFIX);
        exclusiveForParam.add(OaiParameterName.FROM);
        exclusiveForParam.add(OaiParameterName.IDENTIFIER);
        exclusiveForParam.add(OaiParameterName.SET);
        exclusiveForParam.add(OaiParameterName.UNTIL);
        exclusiveParameters.put(OaiParameterName.RESUMPTION_TOKEN, exclusiveForParam);
        // from
        exclusiveForParam = new HashSet<>();
        exclusiveForParam.add(OaiParameterName.RESUMPTION_TOKEN);
        exclusiveForParam.add(OaiParameterName.IDENTIFIER);
        exclusiveParameters.put(OaiParameterName.FROM, exclusiveForParam);
        // until
        exclusiveParameters.put(OaiParameterName.UNTIL, exclusiveForParam);
        // set
        exclusiveParameters.put(OaiParameterName.SET, exclusiveForParam);
    }

    private OaiPmhRequestFactory() {}

    public static void validateVerb(String verb) throws BadVerbException {
        if (verb == null || !validVerbs.contains(verb)) {
            throw new BadVerbException("Verb \"" + verb + "\" is invalid!");
        }
    }

    private static void validateExclusiveParameters(OaiParameterName parameterName, Set<OaiParameterName> currentParameters) throws BadArgumentException {
        Set<OaiParameterName> exclusive = exclusiveParameters.get(parameterName);
        if (exclusive == null || currentParameters == null) {
            // there are no exclusive parameters for the parameter being validated
            return;
        }
        for (OaiParameterName exclusiveParameter : exclusive) {
            if (currentParameters.contains(exclusiveParameter)) {
                throw new BadArgumentException(String.format(PARAMETER_INFO, parameterName.toString()) + "cannot be used with parameter \"" + exclusiveParameter.toString() + "\"");
            }
        }
    }

    private static void validateVerbParameter(String verb, OaiParameterName parameterName) throws BadArgumentException {
        Set<OaiParameterName> valid = validVerbParameters.get(verb);
        if (valid != null && !valid.contains(parameterName)) {
            throw new BadArgumentException(String.format(PARAMETER_INFO, parameterName.toString()) + "is illegal for verb \"" + verb + "\"");
        }
    }

    /**
     * Validates all the parameter names that are present in the request.
     * Request must have format: param1=value1&param2=value2&...&paramN=valueN
     * If any of the parameters is not recognized as an OAI-PMH request parameter BadArgumentException is thrown.
     * Valid parameter names are: {@link OaiParameterName}
     *
     * @param request request string from HttpRequest
     * @throws BadArgumentException
     */
    public static void validateParameterNames(String request) throws BadArgumentException, BadVerbException {
        Map<OaiParameterName, String> parameters = prepareParameters(request, false);
        validateMandatoryParameters(parameters);
        validateDateParameters(parameters.get(OaiParameterName.FROM), parameters.get(OaiParameterName.UNTIL));
    }

    private static void validateMandatoryParameters(Map<OaiParameterName, String> parameters) throws BadVerbException, BadArgumentException {
        String verb = getVerb(parameters);
        Set<OaiParameterName> mandatoryParameters = mandatoryVerbParameters.get(verb);
        if (mandatoryParameters != null && !mandatoryParameters.isEmpty()) {
            for (OaiParameterName parameterName : mandatoryParameters) {
                checkMandatoryParameter(parameters, parameterName);
            }
        }
    }

    private static void checkMandatoryParameter(Map<OaiParameterName, String> parameters, OaiParameterName parameterName) throws BadArgumentException {
        if (!parameters.containsKey(parameterName)) {
            // when this parameter is missing maybe it is one from the exclusive
            Set<OaiParameterName> exclusive = exclusiveParameters.get(parameterName);
            boolean found = false;
            if (exclusive != null) {
                for (OaiParameterName exclusiveParameterName : exclusive) {
                    if (parameters.containsKey(exclusiveParameterName)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                throw new BadArgumentException("Required parameter \"" + parameterName + "\" is missing.");
            }
        }
    }

    private static String getVerb(Map<OaiParameterName, String> parameters) throws BadVerbException {
        if (parameters == null || parameters.isEmpty()) {
            throw new BadVerbException("Verb is missing.");
        }
        String verb = parameters.get(OaiParameterName.VERB);
        if (verb == null || verb.isEmpty()) {
            throw new BadVerbException("Verb is missing.");
        }
        return verb;
    }

    /**
     * Validates from and until dates parameters. Detects whether they have wrong format or from is later than until.
     *
     * @param from  from date
     * @param until until date
     * @throws BadArgumentException
     */
    private static void validateDateParameters(String from, String until) throws BadArgumentException {
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
     * Validates a request parameter and detects whether it is empty or was specified multiple times.
     * When any of these defects occurs BadArgumentException is thrown.
     *
     * @param name  name of the parameter that is verified
     * @param value parameter value
     * @throws BadArgumentException
     */
    private static void validateParameter(String verb, String name, String value) throws BadArgumentException {
        if (!OaiParameterName.contains(name)) {
            throw new BadArgumentException("Parameter name \"" + name + "\" is not supported!");
        }

        if (verb != null) {
            validateVerbParameter(verb, OaiParameterName.fromString(name));
        }

        // empty
        if (value == null || value.isEmpty()) {
            throw new BadArgumentException(String.format(PARAMETER_INFO, name) + "cannot be empty");
        }

        // specified multiple times
        String[] split = value.split(",");
        validateMultipleParameter(split.length > 1, name);
    }

    /**
     * Prepares the map associating parameter name with its value from the request string.
     * When any of the parameters is not recognized as valid OAI-PMH parameter then the BadArgumentException is thrown.
     *
     * @param request request string from the HttpRequest
     * @return map associating parameter name with its value
     * @throws BadArgumentException
     */
    private static Map<OaiParameterName, String> prepareParameters(String request, boolean ignoreErrors) throws BadArgumentException {
        Map<OaiParameterName, String> parameters = new HashMap<>();

        if (request == null) {
            return parameters;
        }

        String[] arguments = request.split("&");
        for (String argument : arguments) {
            String[] paramValue = argument.split("=");
            if (paramValue.length == 2) {
                prepareNormalParameter(ignoreErrors, parameters, paramValue[0], paramValue[1]);
            } else if (paramValue.length == 1) {
                prepareParameterWithoutValue(ignoreErrors, parameters, paramValue[0]);
            } else {
                String value = argument.substring(argument.indexOf('=') + 1);
                try {
                    validateParameter(parameters.get(OaiParameterName.VERB), paramValue[0], value);
                    validateMultipleParameter(parameters.containsKey(OaiParameterName.fromString(paramValue[0])), paramValue[0]);
                    validateExclusiveParameters(OaiParameterName.fromString(paramValue[0]), parameters.keySet());
                } catch (BadArgumentException e) {
                    if (!ignoreErrors) {
                        throw e;
                    }
                }
                try {
                    parameters.put(OaiParameterName.fromString(paramValue[0]), URLDecoder.decode(value, StandardCharsets.UTF_8.name()));
                } catch (IllegalArgumentException e) {
                    // here we just skip adding the parameter to the map because this exception can be caught only when ignoreErrors is true
                } catch (UnsupportedEncodingException e) {
                    throw new BadArgumentException("The value of the parameter \"" + paramValue[0] + "\" has wrong syntax: \"" + value);
                }
            }
        }

        return parameters;
    }

    private static void prepareParameterWithoutValue(boolean ignoreErrors, Map<OaiParameterName, String> parameters, String name) throws BadArgumentException {
        try {
            validateParameter(parameters.get(OaiParameterName.VERB), name, null);
            validateExclusiveParameters(OaiParameterName.fromString(name), parameters.keySet());
        } catch (BadArgumentException e) {
            if (!ignoreErrors) {
                throw e;
            }
        }
        try {
            parameters.put(OaiParameterName.fromString(name), "");
        } catch (IllegalArgumentException e) {
            // here we just skip adding the parameter to the map because this exception can be caught only when ignoreErrors is true
        }
    }

    private static void prepareNormalParameter(boolean ignoreErrors, Map<OaiParameterName, String> parameters, String paramName, String paramValue) throws BadArgumentException {
        try {
            validateParameter(parameters.get(OaiParameterName.VERB), paramName, paramValue);
            validateMultipleParameter(parameters.containsKey(OaiParameterName.fromString(paramName)), paramValue);
            validateExclusiveParameters(OaiParameterName.fromString(paramName), parameters.keySet());
        } catch (BadArgumentException e) {
            if (!ignoreErrors) {
                throw e;
            }
        }
        try {
            parameters.put(OaiParameterName.fromString(paramName), URLDecoder.decode(paramValue, StandardCharsets.UTF_8.name()));
        } catch (IllegalArgumentException e) {
            // here we just skip adding the parameter to the map because this exception can be caught only when ignoreErrors is true
        } catch (UnsupportedEncodingException e) {
            throw new BadArgumentException("The value of the parameter \"" + paramName + "\" has wrong syntax: \"" + paramValue);
        }
    }

    /**
     * Throw an exception when parameter name has already been used.
     *
     * @param used parameter was used
     * @param name name of parameter
     * @throws BadArgumentException
     */
    private static void validateMultipleParameter(boolean used, String name) throws BadArgumentException {
        if (used) {
            throw new BadArgumentException(String.format(PARAMETER_INFO, name) + "can be specified only once.");
        }
    }

    /**
     * Create request of the specific class according to the verb specified in the request string. When any of the parameters
     * specified in the request is not recognized as a valid OAI-PMH parameter BadArgumentException is thrown.
     * If ignoreErrors is true then the request object is created even though it might be partially invalid. Use it ONLY for
     * error reporting.
     *
     * @param baseUrl request url
     * @param request request string
     * @param ignoreErrors when true validation does not throw exception but it creates the request object (which may be partially invalid)
     * @return OAIRequest subclass specific to the specified verb
     * @throws BadArgumentException
     */
    public static OAIRequest createRequest(String baseUrl, String request, boolean ignoreErrors) throws BadArgumentException {
        Map<OaiParameterName, String> parameters = prepareParameters(request, ignoreErrors);

        String verb = parameters.get(OaiParameterName.VERB);
        if (verb == null) {
            if (ignoreErrors) {
                return new OAIRequest(null, baseUrl);
            }
            throw new BadArgumentException("Verb parameter is missing...");
        }

        if (Identify.class.getSimpleName().equals(verb)) {
            return createIdentifyRequest(baseUrl);
        }

        if (ListIdentifiers.class.getSimpleName().equals(verb)) {
            return createListIdentifiersRequest(baseUrl, parameters);
        }

        if (GetRecord.class.getSimpleName().equals(verb)) {
            return createGetRecordRequest(baseUrl, parameters.get(OaiParameterName.METADATA_PREFIX), parameters.get(OaiParameterName.IDENTIFIER));
        }

        if (ListSets.class.getSimpleName().equals(verb)) {
            return createListSetsRequest(baseUrl, parameters.get(OaiParameterName.RESUMPTION_TOKEN));
        }

        if (ListMetadataFormats.class.getSimpleName().equals(verb)) {
            return createListMetadataFormatsRequest(baseUrl, parameters.get(OaiParameterName.IDENTIFIER));
        }

        if (!ignoreErrors) {
            throw new BadArgumentException("Unsupported verb.");
        }
        // in this case just create a general request object with verb and url only.
        return new OAIRequest(verb, baseUrl);
    }

    public static ListSetsRequest createListSetsRequest(String baseUrl, String resumptionToken) {
        if (resumptionToken != null) {
            return new ListSetsRequest(ListSets.class.getSimpleName(), baseUrl, resumptionToken);
        }
        return new ListSetsRequest(ListSets.class.getSimpleName(), baseUrl);
    }

    private static ListIdentifiersRequest createListIdentifiersRequest(String baseUrl, Map<OaiParameterName, String> parameters) throws BadArgumentException {
        if (parameters.containsKey(OaiParameterName.RESUMPTION_TOKEN)) {
            return createListIdentifiersRequest(baseUrl, parameters.get(OaiParameterName.RESUMPTION_TOKEN));
        }
        if (parameters.containsKey(OaiParameterName.METADATA_PREFIX)) {
            return createListIdentifiersRequest(baseUrl, parameters.get(OaiParameterName.METADATA_PREFIX),
                    parameters.get(OaiParameterName.SET),
                    parameters.get(OaiParameterName.FROM),
                    parameters.get(OaiParameterName.UNTIL));
        }
        // when key parameters are missing return a basic list identifiers request object
        return new ListIdentifiersRequest(parameters.get(OaiParameterName.VERB), baseUrl);
    }

    public static ListIdentifiersRequest createListIdentifiersRequest(String baseUrl, String metadataPrefix, String set, String from, String until) {
        return new ListIdentifiersRequest(ListIdentifiers.class.getSimpleName(), baseUrl, metadataPrefix, set, from, until);
    }

    public static ListIdentifiersRequest createListIdentifiersRequest(String baseUrl, String resumptionToken) {
        return new ListIdentifiersRequest(ListIdentifiers.class.getSimpleName(), baseUrl, resumptionToken);
    }

    public static IdentifyRequest createIdentifyRequest(String baseUrl) {
        return new IdentifyRequest(Identify.class.getSimpleName(), baseUrl);
    }

    public static GetRecordRequest createGetRecordRequest(String baseUrl, String metadataPrefix, String identifier) {
        return new GetRecordRequest(GetRecord.class.getSimpleName(), baseUrl, metadataPrefix, identifier);
    }

    public static ListMetadataFormatsRequest createListMetadataFormatsRequest(String baseUrl, String identifier) {
        return new ListMetadataFormatsRequest(ListMetadataFormats.class.getSimpleName(), baseUrl, identifier);
    }
}
