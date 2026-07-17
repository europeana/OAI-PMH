package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.*;
import eu.europeana.oaipmh.model.request.*;
import eu.europeana.oaipmh.service.exception.BadArgumentException;
import java.util.*;

import static eu.europeana.oaipmh.service.OaiParameterName.*;

import static eu.europeana.oaipmh.service.exception.ErrorConstants.*;

public class OaiPmhRequestFactory {

    private OaiPmhRequestFactory() {}

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
        Map<OaiParameterName, String> parameters = OaiPmhValidationService.prepareParameters(request, ignoreErrors);

        String verb = parameters.get(VERB);
        if (verb == null) {
            if (ignoreErrors) {
                return new ErrorRequest(null, baseUrl);
            }
            throw new BadArgumentException(BAD_ARGUMENT_VERB_MISSING_MSG);
        }

        if (Identify.class.getSimpleName().equals(verb)) {
            return createIdentifyRequest(baseUrl);
        }

        if (ListIdentifiers.class.getSimpleName().equals(verb)) {
            return createListIdentifiersRequest(baseUrl, parameters);
        }

        if (GetRecord.class.getSimpleName().equals(verb)) {
            return createGetRecordRequest(baseUrl, parameters.get(METADATA_PREFIX), parameters.get(IDENTIFIER));
        }

        if (ListSets.class.getSimpleName().equals(verb)) {
            return createListSetsRequest(baseUrl, parameters);
        }

        if (ListMetadataFormats.class.getSimpleName().equals(verb)) {
            return createListMetadataFormatsRequest(baseUrl, parameters.get(IDENTIFIER));
        }

        if (ListRecords.class.getSimpleName().equals(verb)) {
            return createListRecordsRequest(baseUrl, parameters);
        }

        if (!ignoreErrors) {
            throw new BadArgumentException(BAD_ARGUMENT_VERB_MSG);
        }
        // in this case just create a general request object with verb and url only.
        return new ErrorRequest(verb, baseUrl);
    }

    private static ListSetsRequest createListSetsRequest(
            String baseUrl, Map<OaiParameterName, String> parameters) {
        if (parameters.containsKey(RESUMPTION_TOKEN)) {
            return createListSetsRequest(baseUrl, parameters.get(RESUMPTION_TOKEN));
        }
        if (parameters.containsKey(METADATA_PREFIX)) {
            return createListSetsRequest(baseUrl,
                    parameters.get(FROM),
                    parameters.get(UNTIL));
        }
        // when key parameters are missing return a basic list records request object
        return new ListSetsRequest(parameters.get(VERB), baseUrl);
    }

    public static ListSetsRequest createListSetsRequest(
            String baseUrl, String resumptionToken) {
       return new ListSetsRequest(ListSets.class.getSimpleName(), baseUrl, resumptionToken);

    }

    public static ListSetsRequest createListSetsRequest(
            String baseUrl, String from, String until) {
        return new ListSetsRequest(ListSets.class.getSimpleName(), baseUrl, from, until);
    }

    private static ListIdentifiersRequest createListIdentifiersRequest(String baseUrl, Map<OaiParameterName, String> parameters) {
        if (parameters.containsKey(RESUMPTION_TOKEN)) {
            return createListIdentifiersRequest(baseUrl, parameters.get(RESUMPTION_TOKEN));
        }
        if (parameters.containsKey(METADATA_PREFIX)) {
            return createListIdentifiersRequest(baseUrl, parameters.get(METADATA_PREFIX),
                    parameters.get(SET),
                    parameters.get(FROM),
                    parameters.get(UNTIL));
        }
        // when key parameters are missing return a basic list identifiers request object
        return new ListIdentifiersRequest(parameters.get(VERB), baseUrl);
    }

    public static ListIdentifiersRequest createListIdentifiersRequest(
            String baseUrl, String metadataPrefix, String set, String from, String until) {
        return new ListIdentifiersRequest(ListIdentifiers.class.getSimpleName(), baseUrl, metadataPrefix, set, from, until);
    }

    public static ListIdentifiersRequest createListIdentifiersRequest(
            String baseUrl, String resumptionToken) {
        return new ListIdentifiersRequest(ListIdentifiers.class.getSimpleName(), baseUrl, resumptionToken);
    }

    public static IdentifyRequest createIdentifyRequest(String baseUrl) {
        return new IdentifyRequest(Identify.class.getSimpleName(), baseUrl);
    }

    public static GetRecordRequest createGetRecordRequest(
            String baseUrl, String metadataPrefix, String identifier) {
        return new GetRecordRequest(GetRecord.class.getSimpleName(), baseUrl, metadataPrefix, identifier);
    }

    public static ListMetadataFormatsRequest createListMetadataFormatsRequest(
            String baseUrl, String identifier) {
        return new ListMetadataFormatsRequest(ListMetadataFormats.class.getSimpleName(), baseUrl, identifier);
    }

    private static ListRecordsRequest createListRecordsRequest(
            String baseUrl, Map<OaiParameterName, String> parameters) {
        if (parameters.containsKey(RESUMPTION_TOKEN)) {
            return createListRecordsRequest(baseUrl, parameters.get(RESUMPTION_TOKEN));
        }
        if (parameters.containsKey(METADATA_PREFIX)) {
            return createListRecordsRequest(baseUrl, parameters.get(METADATA_PREFIX),
                    parameters.get(SET),
                    parameters.get(FROM),
                    parameters.get(UNTIL));
        }
        // when key parameters are missing return a basic list records request object
        return new ListRecordsRequest(parameters.get(VERB), baseUrl);
    }

    public static ListRecordsRequest createListRecordsRequest(
            String baseUrl, String metadataPrefix, String set
          , String from, String until) {
        return new ListRecordsRequest(ListRecords.class.getSimpleName(), baseUrl, metadataPrefix, set, from, until);
    }

    public static ListRecordsRequest createListRecordsRequest(
            String baseUrl, String resumptionToken) {
        return new ListRecordsRequest(ListRecords.class.getSimpleName(), baseUrl, resumptionToken);
    }
}
