package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.request.IdentifyRequest;
import eu.europeana.oaipmh.model.request.ListIdentifiersRequest;
import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.service.exception.BadArgumentException;
import org.junit.Test;

import static org.junit.Assert.*;

public class OaiPmhRequestFactoryTest {
    private static final String VALID_REQUEST = "verb=ListIdentifiers&metadataPrefix=edm";

    private static final String NO_VERB_REQUEST = "metadataPrefix=edm&set=abc";

    private static final String BASE_URL = "http://localhost";

    private static final String EMPTY_PARAMETER_REQUEST = "verb=ListIdentifiers&set=";

    private static final String INVALID_PARAMETER_NAME_REQUEST = "verbs=ListIdentifiers";

    private static final String MULTI_PARAMETER_REQUEST_1 = "verb=ListIdentifiers&set=abc&set=vbn";

    private static final String MULTI_PARAMETER_REQUEST_2 = "verb=ListIdentifiers&set=abc,vbn";

    private static final String UNSUPPORTED_VERB = "verb=XYZ";

    private static final String IDENTIFY_REQUEST = "verb=Identify";

    private static final String LIST_IDENTIFIERS_GENERAL_REQUEST = "verb=ListIdentifiers&metadataPrefix=edm&set=ABC";

    private static final String EDM_FORMAT = "edm";

    private static final String SET_NAME = "ABC";

    private static final String VERB_LIST_IDENTIFIERS_RESUMPTION_TOKEN_REQUEST = "verb=ListIdentifiers&resumptionToken=JHJHGHSAGHSJGAJ";

    private static final String RESUMPTION_TOKEN = "JHJHGHSAGHSJGAJ";

    @Test
    public void validateParameterNamesValid() {
        try {
            OaiPmhRequestFactory.validateParameterNames(VALID_REQUEST);
        } catch (BadArgumentException e) {
            fail();
        }
    }

    @Test(expected = BadArgumentException.class)
    public void validateParameterNamesEmptyOrNull() throws BadArgumentException {
        OaiPmhRequestFactory.validateParameterNames(EMPTY_PARAMETER_REQUEST);
    }

    @Test(expected = BadArgumentException.class)
    public void validateParameterNamesInvalidName() throws BadArgumentException {
        OaiPmhRequestFactory.validateParameterNames(INVALID_PARAMETER_NAME_REQUEST);
    }

    @Test(expected = BadArgumentException.class)
    public void validateParameterNamesMultiParameter() throws BadArgumentException {
        OaiPmhRequestFactory.validateParameterNames(MULTI_PARAMETER_REQUEST_1);
        OaiPmhRequestFactory.validateParameterNames(MULTI_PARAMETER_REQUEST_2);
    }

    @Test(expected = BadArgumentException.class)
    public void validateParameterNamesWrongDates() throws BadArgumentException {
        OaiPmhRequestFactory.validateParameterNames("verb=ListIdentifiers&from=2000-01-01T00:00:00Z&until=1995-01-01T00:00:00Z");
    }

    @Test(expected = BadArgumentException.class)
    public void validateCreateRequestNoVerb() throws BadArgumentException {
        OaiPmhRequestFactory.createRequest(BASE_URL, NO_VERB_REQUEST, false);
    }

    @Test
    public void validateCreateRequestNoVerbIgnoreErrors() {
        try {
            OAIRequest request = OaiPmhRequestFactory.createRequest(BASE_URL, NO_VERB_REQUEST, true);
            assertNotNull(request);
            assertNull(request.getVerb());
        } catch (BadArgumentException e) {
            fail();
        }
    }

    @Test(expected = BadArgumentException.class)
    public void validateCreateRequestUnsupportedVerb() throws BadArgumentException {
        OaiPmhRequestFactory.createRequest(BASE_URL, UNSUPPORTED_VERB, false);
    }

    @Test
    public void validateCreateRequestUnsupportedVerbIgnoreErrors() {
        try {
            OAIRequest request = OaiPmhRequestFactory.createRequest(BASE_URL, UNSUPPORTED_VERB, true);
            assertNotNull(request);
            assertTrue(UNSUPPORTED_VERB.endsWith(request.getVerb()));
        } catch (BadArgumentException e) {
            fail();
        }
    }

    @Test
    public void createRequestWithIdentify() {
        try {
            OAIRequest request = OaiPmhRequestFactory.createRequest(BASE_URL, IDENTIFY_REQUEST, false);
            assertIdentify(request);
        } catch (BadArgumentException e) {
            fail();
        }
    }

    private void assertIdentify(OAIRequest request) {
        assertNotNull(request);
        assertTrue(request instanceof IdentifyRequest);
    }

    @Test
    public void createRequestWithListIdentifiersGeneral() {
        try {
            OAIRequest request = OaiPmhRequestFactory.createRequest(BASE_URL, LIST_IDENTIFIERS_GENERAL_REQUEST, false);
            assertListIdentifiersGeneral(request);
        } catch (BadArgumentException e) {
            fail();
        }
    }

    private void assertListIdentifiersGeneral(OAIRequest request) {
        assertNotNull(request);
        assertTrue(request instanceof ListIdentifiersRequest);
        assertEquals(EDM_FORMAT, ((ListIdentifiersRequest) request).getMetadataPrefix());
        assertEquals(SET_NAME, ((ListIdentifiersRequest) request).getSet());
        assertNull(((ListIdentifiersRequest)request).getResumptionToken());
    }

    @Test
    public void createRequestWithListIdentifiersResumptionToken() {
        try {
            OAIRequest request = OaiPmhRequestFactory.createRequest(BASE_URL, VERB_LIST_IDENTIFIERS_RESUMPTION_TOKEN_REQUEST, false);
            assertListIdentifiersWithResumptionToken(request);
        } catch (BadArgumentException e) {
            fail();
        }
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
    public void createListIdentifiersRequestGeneral() {
        OAIRequest request = OaiPmhRequestFactory.createListIdentifiersRequest(BASE_URL, EDM_FORMAT, SET_NAME, null, null);
        assertListIdentifiersGeneral(request);
    }

    @Test
    public void createListIdentifiersRequestWithResumptionToken() {
        OAIRequest request = OaiPmhRequestFactory.createListIdentifiersRequest(BASE_URL, RESUMPTION_TOKEN);
        assertListIdentifiersWithResumptionToken(request);
    }

    @Test
    public void createIdentifyRequest() {
        OAIRequest request = OaiPmhRequestFactory.createIdentifyRequest(BASE_URL);
        assertIdentify(request);
    }
}