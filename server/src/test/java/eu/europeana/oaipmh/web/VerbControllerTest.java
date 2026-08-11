package eu.europeana.oaipmh.web;

import eu.europeana.oaipmh.AbstractIntegrationIT;
import eu.europeana.oaipmh.model.request.*;
import eu.europeana.oaipmh.util.DateConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for validating the behavior and functionality of the VerbController.
 * This class contains integration tests for various OAI-PMH verbs and ensures that
 * the VerbController handles requests and responses correctly.
 *
 * NOTE: As we use StreamingResponseBody (which is async),
 *       the response body here will be null most of the time. hence not checking any serialisation of the responses here
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VerbControllerTest extends AbstractIntegrationIT {

    // verb=Identify tests
    @Test
    void shouldReturnXmlForGetRequestWithTextXmlAcceptHeader() throws Exception {
        mockMvc.perform(get(OAI_ENDPOINT)
                        .accept(MediaType.TEXT_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML));
    }

    @Test
    void shouldReturnXmlForPostRequestWithApplicationXmlAcceptHeader() throws Exception {
        mockMvc.perform(post(OAI_ENDPOINT)
                        .accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    @Test
    void shouldDefaultToApplicationXmlWhenNoAcceptHeaderProvided() throws Exception {
        mockMvc.perform(post(OAI_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    // metadatFormats tests
    @Test
    public void testListMetadataFormats() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListMetadataFormats").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(get("/oai?verb=ListMetadataFormats").accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));

        this.mockMvc.perform(get("/oai?verb=ListMetadataFormats"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));
    }

    @Test
    public void testListMetadataFormatsWrongArgument() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListMetadataFormats&resumptionToken=XXX").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListMetadataFormatsWrongMethod() throws Exception {
        this.mockMvc.perform(put("/oai?verb=ListMetadataFormats").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    // GetRecord tests
    @Test
    public void testGetRecord() throws Exception {
        this.mockMvc.perform(get("/oai?verb=GetRecord&metadataPrefix=edm&identifier=" +RECORD_ID_876_1).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(get("/oai?verb=GetRecord&metadataPrefix=edm&identifier="+ RECORD_ID_2064125_1).accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));

        this.mockMvc.perform(get("/oai?verb=GetRecord&metadataPrefix=edm&identifier=" +RECORD_ID_401_1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));
    }

    // ListIdentifier tests

    @Test
    public void testListIdentifiers() throws Exception {
        // without set,from and until
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm")
                        .accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        // with set
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set="+SET_1)
                        .accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set="+SET_5)
                        .accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));

        // with from until
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from="+DATE_1+"&until="+DATE_3)
                        .accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));


        // with set, from and until
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set="+SET_5+"&from="+DATE_2+"&until="+Instant.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    @Test
    public void testListIdentifiersWithResumptionToken() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&resumptionToken=" + LIST_IDENTIFIERS_TOKEN).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithCorruptedResumptionToken() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&resumptionToken=" + LIST_IDENTIFIERS_CORRUPTED_TOKEN).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }


    @Test
    public void testInvalidVerb() throws Exception {
        this.mockMvc.perform(get("/oai?verb=XXX").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithEmptySet() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithMultipleSets() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set=123&set=1234").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithEmptyFrom() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithExclusiveParameters() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&resumptionToken=ABBB").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithExclusiveParametersForToken() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&resumptionToken=ABBB&set=123").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithMultipleFroms() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2017-02-02T01:03:00Z&from=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithEmptyUntil() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&until=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithMultipleUntils() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&until=2017-02-02T01:03:00Z&until=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithIncorrectFrom() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2017.02.02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithIncorrectUntil() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&until=2017.02.02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithIncorrectPeriod() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2018-02-02T01:03:00Z&until=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithIncorrectParameterName() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2017-02-02T01:03:00Z&to=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithExpiredResumptionToken() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&resumptionToken=" + LIST_RECORDS_TOKEN).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(get("/oai?verb=ListRecords&resumptionToken=" + LIST_RECORDS_TOKEN).accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MEDIA_TYPE_TEXT_XML));

        this.mockMvc.perform(get("/oai?verb=ListRecords&resumptionToken=" + LIST_RECORDS_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithCorruptedResumptionToken() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&resumptionToken=" + LIST_RECORDS_CORRUPTED_TOKEN).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecords() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&set=2026011&from=2017-02-02T01:03:00Z&until=2017-03-02T01:03:00Z")
                        .accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&set=2026011&from=2017-02-02T01:03:00Z&until=2017-03-02T01:03:00Z")
                        .accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));

        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&set=2026011&from=2017-02-02T01:03:00Z&until=2017-03-02T01:03:00Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));
    }

    @Test
    public void testListRecordsWithEmptySet() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&set=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithMultipleSets() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&set=123&set=345").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithEmptyFrom() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&from=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithExclusiveParameters() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&resumptionToken=ABBB").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithExclusiveParametersForToken() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&resumptionToken=ABBB&set=1232").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithMultipleFroms() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&from=2017-02-02T01:03:00Z&from=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithEmptyUntil() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&until=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithMultipleUntils() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&until=2017-02-02T01:03:00Z&until=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithIncorrectFrom() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&from=2017.02.02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithIncorrectUntil() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&until=2017.02.02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithIncorrectPeriod() throws Exception {

        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&from=2018-02-02T01:03:00Z&until=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithIncorrectParameterName() throws Exception {
        this.mockMvc.perform(get("/oai?verb=ListRecords&metadataPrefix=edm&from=2017-02-02T01:03:00Z&to=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MEDIA_TYPE_TEXT_XML));
    }
}
