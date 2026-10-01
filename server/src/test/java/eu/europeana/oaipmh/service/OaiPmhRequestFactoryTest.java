package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.AbstractIntegrationIT;
import eu.europeana.oaipmh.model.request.IdentifyRequest;
import eu.europeana.oaipmh.model.request.ListIdentifiersRequest;
import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.service.exception.BadArgumentException;
import eu.europeana.oaipmh.service.exception.BadVerbException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

/**
 * Test class for validating the behavior of the {@link OaiPmhRequestFactory} and related
 * services such as {@link OaiPmhValidationService}.
 */
public class OaiPmhRequestFactoryTest extends AbstractIntegrationIT {

    @Test
    void validateParameterNamesValid() {
        OaiPmhValidationService.validateParameterNames(VALID_REQUEST);
    }

    @Test
    void validateUnsupportedVerb() throws BadVerbException, BadArgumentException {
        BadVerbException ex1 = assertThrows(BadVerbException.class, ()
                ->  OaiPmhValidationService.validateVerb(UNSUPPORTED_VERB));

        assertEquals("Verb \"GetIdentifiers\" is invalid!", ex1.getMessage());

        BadVerbException ex2 = assertThrows(BadVerbException.class, ()
                ->          OaiPmhValidationService.validateVerb(UNSUPPORTED_VERB_REQUEST));

        assertEquals("Verb \"verb=XYZ\" is invalid!", ex2.getMessage());
    }

    @Test
    void validateMandatoryParametersListIdentifiers() throws BadVerbException, BadArgumentException {
        OaiPmhValidationService.validateParameterNames(LIST_IDENTIFIERS_GENERAL_REQUEST); // OK

        BadArgumentException ex2 = assertThrows(BadArgumentException.class, ()
                ->  OaiPmhValidationService.validateParameterNames(LIST_IDENTIFIERS_NO_MANDATORY_REQUEST)); // Exception

        assertEquals("Required parameter \"metadataPrefix\" is missing.", ex2.getMessage());
    }

    @Test
    void validateMandatoryParametersGetRecord() throws BadVerbException, BadArgumentException {
        OaiPmhValidationService.validateParameterNames(GET_RECORD_GENERAL_REQUEST); // OK
        BadArgumentException ex2 = assertThrows(BadArgumentException.class, ()
                -> OaiPmhValidationService.validateParameterNames(GET_RECORD_NO_MANDATORY_REQUEST)); // Exception

        assertEquals("Required parameter \"identifier\" is missing.", ex2.getMessage());
    }

    @Test
    void validateMandatoryParametersIdentify() throws BadVerbException, BadArgumentException {
        OaiPmhValidationService.validateParameterNames(IDENTIFY_REQUEST); // OK always
    }

    @Test
    void validateParameterNamesEmptyOrNull() throws BadArgumentException, BadVerbException {
        BadArgumentException ex2 = assertThrows(BadArgumentException.class, ()
                ->        OaiPmhValidationService.validateParameterNames(EMPTY_PARAMETER_NAME_REQUEST)); // Exception

        assertEquals("Empty parameter!", ex2.getMessage());
    }

    @Test
    void validateParameterValueEmptyOrNull() throws BadArgumentException, BadVerbException {
        BadArgumentException ex2 = assertThrows(BadArgumentException.class, ()
                -> OaiPmhValidationService.validateParameterNames(EMPTY_PARAMETER_VALUE_REQUEST)); // Exception

        assertEquals("Parameter \"set\" requires a value", ex2.getMessage());
    }

    @Test
    void validateParameterNamesInvalidName() throws BadArgumentException, BadVerbException {
        BadArgumentException ex2 = assertThrows(BadArgumentException.class, ()
                -> OaiPmhValidationService.validateParameterNames(INVALID_PARAMETER_NAME_REQUEST)); // Exception

        assertEquals("Parameter \"verbs\" is not supported!", ex2.getMessage());
    }

    @Test
    void validateParameterNamesMultiParameter() throws BadArgumentException, BadVerbException {
        BadArgumentException ex1 = assertThrows(BadArgumentException.class, ()
                ->         OaiPmhValidationService.validateParameterNames(MULTI_PARAMETER_REQUEST_1)); // Exception

        assertEquals("Parameter \"set\" can be specified only once.", ex1.getMessage());

        BadArgumentException ex2 = assertThrows(BadArgumentException.class, ()
                ->         OaiPmhValidationService.validateParameterNames(MULTI_PARAMETER_REQUEST_2)); // Exception

        assertEquals("Parameter \"set\" can be specified only once.", ex2.getMessage());
    }

    @Test
    void validateParameterNamesWrongDates() throws BadArgumentException, BadVerbException {
        BadArgumentException ex1 = assertThrows(BadArgumentException.class, ()
                -> OaiPmhValidationService.
                validateParameterNames("verb=ListIdentifiers&from=2000-01-01T00:00:00Z&until=1995-01-01T00:00:00Z")); // Exception

        assertEquals("Required parameter \"metadataPrefix\" is missing.", ex1.getMessage());
    }

    @Test
    void validateCreateRequestNoVerb() throws BadArgumentException {
        BadArgumentException ex1 = assertThrows(BadArgumentException.class, ()
                ->         OaiPmhRequestFactory.createRequest(BASE_URL, NO_VERB_REQUEST, false)); // Exception

        assertEquals("Verb parameter is missing...", ex1.getMessage());
    }

    @Test
    void validateCreateRequestNoVerbIgnoreErrors() {
        OAIRequest request = OaiPmhRequestFactory.createRequest(BASE_URL, NO_VERB_REQUEST, true);
        assertNotNull(request);
        assertNull(request.getVerb());

    }

    @Test
    void validateCreateRequestUnsupportedVerb() throws BadArgumentException {
        BadArgumentException ex1 = assertThrows(BadArgumentException.class, ()
                -> OaiPmhRequestFactory.createRequest(BASE_URL, UNSUPPORTED_VERB_REQUEST, false)); // Exception

        assertEquals("Unsupported verb.", ex1.getMessage());
    }

    @Test
    void validateCreateRequestUnsupportedVerbIgnoreErrors() {
        OAIRequest request = OaiPmhRequestFactory.createRequest(BASE_URL, UNSUPPORTED_VERB_REQUEST, true);
        assertNotNull(request);
        assertTrue(UNSUPPORTED_VERB_REQUEST.endsWith(request.getVerb()));

    }

    @Test
    void createRequestWithIdentify() {
        OAIRequest request = OaiPmhRequestFactory.createRequest(BASE_URL, IDENTIFY_REQUEST, false);
        assertIdentify(request);
    }

    private void assertIdentify(OAIRequest request) {
        assertNotNull(request);
        assertTrue(request instanceof IdentifyRequest);
    }

    @Test
    void createRequestWithListIdentifiersGeneral() {
        OAIRequest request = OaiPmhRequestFactory.createRequest(BASE_URL, LIST_IDENTIFIERS_GENERAL_REQUEST, false);
        assertListIdentifiersGeneral(request);

    }

    private void assertListIdentifiersGeneral(OAIRequest request) {
        assertNotNull(request);
        assertTrue(request instanceof ListIdentifiersRequest);
        assertEquals(EDM_FORMAT, ((ListIdentifiersRequest) request).getMetadataPrefix());
        assertEquals(SET_NAME, ((ListIdentifiersRequest) request).getSet());
        assertNull(((ListIdentifiersRequest)request).getResumptionToken());
    }

    @Test
    void createRequestWithListIdentifiersResumptionToken() {
        OAIRequest request = OaiPmhRequestFactory.createRequest(BASE_URL, VERB_LIST_IDENTIFIERS_RESUMPTION_TOKEN_REQUEST, false);
        assertListIdentifiersWithResumptionToken(request);
    }

    private void assertListIdentifiersWithResumptionToken(OAIRequest request) {
        assertNotNull(request);
        assertTrue(request instanceof ListIdentifiersRequest);
        assertNull(((ListIdentifiersRequest) request).getMetadataPrefix());
        assertNull(((ListIdentifiersRequest) request).getSet());
        assertNull(((ListIdentifiersRequest) request).getFrom());
        assertNull(((ListIdentifiersRequest) request).getUntil());
        assertNotNull(((ListIdentifiersRequest)request).getResumptionToken());
    }

    @Test
    void createListIdentifiersRequestGeneral() {
        OAIRequest request = OaiPmhRequestFactory.createListIdentifiersRequest(BASE_URL, EDM_FORMAT, SET_NAME, null, null);
        assertListIdentifiersGeneral(request);
    }

    @Test
    void createListIdentifiersRequestWithResumptionToken() {
        OAIRequest request = OaiPmhRequestFactory.createListIdentifiersRequest(BASE_URL, RESUMPTION_TOKEN);
        assertListIdentifiersWithResumptionToken(request);
    }

    @Test
    void createIdentifyRequest() {
        OAIRequest request = OaiPmhRequestFactory.createIdentifyRequest(BASE_URL);
        assertIdentify(request);
    }
}