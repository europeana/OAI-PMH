package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.service.exception.BadArgumentException;
import eu.europeana.oaipmh.service.exception.BadVerbException;
import eu.europeana.oaipmh.util.DateConverter;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static eu.europeana.oaipmh.service.OaiParameterName.*;
import static eu.europeana.oaipmh.service.exception.ErrorConstants.*;
import static eu.europeana.oaipmh.service.exception.ErrorConstants.BAD_VERB_MISSING_MSG;

import static eu.europeana.oaipmh.service.OaiPmhValidationParam.*;

public class OaiPmhValidationService {

    /**
     * Validates if the provided verb is contained in the list of valid verbs.
     * Throws a BadVerbException if the verb is null or not a valid verb.
     *
     * @param verb the verb to validate
     * @throws BadVerbException if the verb is null or not present in the list of valid verbs
     */
    public static void validateVerb(String verb) throws BadVerbException {
        if (verb == null || !VALID_VERBS.contains(verb)) {
            throw new BadVerbException(msg(BAD_VERB_INVALID_MSG, verb));
        }
    }

    /**
     * Validates that the given parameter is not used in conjunction with any of its exclusive parameters.
     * If an exclusive parameter is detected in the provided set of current parameters, a BadArgumentException is thrown.
     *
     * @param parameterName the parameter to validate for exclusivity with other parameters
     * @param currentParameters the set of currently used parameters to check against exclusivity rules
     * @throws BadArgumentException if the parameter is used alongside one of its exclusive parameters
     */
    private static void validateExclusiveParameters(OaiParameterName parameterName, Set<OaiParameterName> currentParameters) throws BadArgumentException {
        Set<OaiParameterName> exclusive = EXCLUSIVE_PARAMETERS.get(parameterName);
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

    /**
     * Validates whether the provided parameter is valid for the specified OAI-PMH verb.
     * If the parameter is not valid for the verb, this method throws a BadArgumentException.
     *
     * @param verb the OAI-PMH verb being validated
     * @param parameterName the parameter associated with the verb
     * @throws BadArgumentException if the specified parameter is not valid for the given verb
     */
    private static void validateVerbParameter(String verb, OaiParameterName parameterName) throws BadArgumentException {
        Set<OaiParameterName> valid = VALID_VERB_PARAMETERS.get(verb);
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
        validateDateParameters(parameters.get(FROM), parameters.get(UNTIL));
    }

    /**
     * Validates that all mandatory parameters for the OAI-PMH request verb are present in the provided parameter map.
     * If any mandatory parameter is missing, an exception is thrown.
     *
     * @param parameters a map where the keys are parameter names of type OaiParameterName and the values are their corresponding string values
     * @throws BadVerbException if the verb extracted from the parameters is invalid or not recognized
     * @throws BadArgumentException if any mandatory parameter associated with the verb is missing or invalid
     */
    private static void validateMandatoryParameters(Map<OaiParameterName, String> parameters) throws BadVerbException, BadArgumentException {
        String verb = getVerb(parameters);
        Set<OaiParameterName> mandatoryParameters = MANDATORY_VERB_PARAMETERS.get(verb);
        if (mandatoryParameters != null && !mandatoryParameters.isEmpty()) {
            for (OaiParameterName parameterName : mandatoryParameters) {
                checkMandatoryParameter(parameters, parameterName);
            }
        }
    }

    /**
     * Validates that a mandatory parameter is present in the given map of parameters.
     * If the required parameter is not present, it checks whether one of its exclusive alternatives is included.
     * If neither the parameter nor its exclusive alternatives are found, a {@code BadArgumentException} is thrown.
     *
     * @param parameters     the map containing parameter names and their corresponding values
     * @param parameterName  the name of the mandatory parameter to check for
     * @throws BadArgumentException if the required parameter and its exclusive alternatives are missing
     */
    private static void checkMandatoryParameter(Map<OaiParameterName, String> parameters, OaiParameterName parameterName) throws BadArgumentException {
        if (!parameters.containsKey(parameterName)) {
            Set<OaiParameterName> exclusive = EXCLUSIVE_PARAMETERS.get(parameterName);

            boolean satisfied =
                    parameters.containsKey(parameterName) ||
                            (exclusive != null &&
                                    exclusive.stream().anyMatch(parameters::containsKey));

            if (!satisfied) {
                throw new BadArgumentException("Required parameter \"" + parameterName + "\" is missing.");
            }
        }
    }

    private static String getVerb(Map<OaiParameterName, String> parameters) throws BadVerbException {
        String verb = Optional.ofNullable(parameters)
                .map(p -> p.get(VERB))
                .filter(v -> !v.isBlank())
                .orElseThrow(() ->
                        new BadVerbException(BAD_VERB_MISSING_MSG));

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
        if (StringUtils.isEmpty(name)) {
            throw new BadArgumentException("Empty parameter!");
        }
        if (!contains(name)) {
            throw new BadArgumentException("Parameter \"" + name + "\" is not supported!");
        }

        if (verb != null) {
            validateVerbParameter(verb, fromString(name));
        }

        // empty
        if (value == null || value.isEmpty()) {
            throw new BadArgumentException(String.format(PARAMETER_INFO, name) + "requires a value");
        }

        // specified multiple times
        String[] split = value.split(",");
        validateMultipleParameter(split.length > 1, name);
    }



    private static void validateParameterWithoutValue(boolean ignoreErrors, Map<OaiParameterName, String> parameters, String name) throws BadArgumentException {
        try {
            validateParameter(parameters.get(VERB), name, null);
            validateExclusiveParameters(fromString(name), parameters.keySet());
        } catch (BadArgumentException e) {
            if (!ignoreErrors) {
                throw e;
            }
        }
        try {
            parameters.put(fromString(name), "");
        } catch (IllegalArgumentException e) {
            // here we just skip adding the parameter to the map because this exception can be caught only when ignoreErrors is true
        }
    }

    private static void validateNormalParameter(boolean ignoreErrors, Map<OaiParameterName, String> parameters, String paramName, String paramValue) throws BadArgumentException {
        try {
            validateParameter(parameters.get(VERB), paramName, paramValue);
            validateMultipleParameter(parameters.containsKey(fromString(paramName)), paramName);
            validateExclusiveParameters(fromString(paramName), parameters.keySet());
        } catch (BadArgumentException e) {
            if (!ignoreErrors) {
                throw e;
            }
        }
        try {
            parameters.put(fromString(paramName), URLDecoder.decode(paramValue, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            // here we just skip adding the parameter to the map because this exception can be caught only when ignoreErrors is true
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
     * Prepares the map associating parameter name with its value from the request string.
     * When any of the parameters is not recognized as valid OAI-PMH parameter then the BadArgumentException is thrown.
     *
     * @param request request string from the HttpRequest
     * @return map associating parameter name with its value
     * @throws BadArgumentException
     */
    public static Map<OaiParameterName, String> prepareParameters(String request, boolean ignoreErrors) throws BadArgumentException {
        Map<OaiParameterName, String> parameters = new EnumMap<>(OaiParameterName.class);

        if (request == null) {
            return parameters;
        }

        String[] arguments = request.split("&");
        for (String argument : arguments) {
            String[] paramValue = argument.split("=");

            if (paramValue.length == 2) {
                validateNormalParameter(ignoreErrors, parameters, paramValue[0], paramValue[1]);
            } else if (paramValue.length == 1) {
                validateParameterWithoutValue(ignoreErrors, parameters, paramValue[0]);
            } else {
                String value = argument.substring(argument.indexOf('=') + 1);
                validateParameterWithoutValue(ignoreErrors, parameters, value);
            }

        }

        return parameters;
    }

}
