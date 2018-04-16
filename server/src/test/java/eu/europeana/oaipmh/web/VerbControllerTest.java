package eu.europeana.oaipmh.web;

import eu.europeana.oaipmh.model.request.GetRecordRequest;
import eu.europeana.oaipmh.model.request.IdentifyRequest;
import eu.europeana.oaipmh.model.request.ListIdentifiersRequest;
import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.service.OaiPmhService;
import eu.europeana.oaipmh.service.exception.BadResumptionToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

/**
 * Test the application's controller
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
@RunWith(MockitoJUnitRunner.class)
public class VerbControllerTest {

    private static final String IDENTIFY_RESPONSE = "<OAI-PMH><responseDate>2018-03-16T10:10:32Z</responseDate><request verb=\"Identify\">\"https://oai.europeana.eu/oai\"</request><Identify><repositoryName>Europeana Repository</repositoryName><earliestDateStamp>2013-02-15T13:04:50Z</earliestDateStamp><deletedRecord>no</deletedRecord><adminEmail>api@europeana.eu</adminEmail></Identify></OAI-PMH>";

    private static final String RECORD_RESPONSE = "<OAI-PMH><responseDate>2018-03-14T13:37:31Z</responseDate><request verb=\"GetRecord\">\"https://oai.europeana.eu/oai\"</request><GetRecord><xmlData><?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"><edm:ProvidedCHO rdf:about=\"http://data.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:WebResource rdf:about=\"http://xantho.lis.upatras.gr/kosmopolis/index.php/xrysallis/article/view/2994\"/><edm:WebResource rdf:about=\"http://xantho.lis.upatras.gr/test2.php?art=2994\"/><ore:Aggregation xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/aggregation/provider/00501/330511407E2E987232571B5CCE38F689018984C8\"><edm:aggregatedCHO rdf:resource=\"http://data.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:dataProvider>University of Patras/Library and Information Center</edm:dataProvider><edm:isShownAt rdf:resource=\"http://xantho.lis.upatras.gr/kosmopolis/index.php/xrysallis/article/view/2994\"/><edm:isShownBy rdf:resource=\"http://xantho.lis.upatras.gr/test2.php?art=2994\"/><edm:object rdf:resource=\"http://xantho.lis.upatras.gr/test2.php?art=2994\"/><edm:provider>University of Patras/Library and Information Center</edm:provider><edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/></ore:Aggregation><ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/proxy/provider/00501/330511407E2E987232571B5CCE38F689018984C8\"><dc:creator xmlns:dc=\"http://purl.org/dc/elements/1.1/\">[Ανωνύμως]</dc:creator><dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\">[1970]</dc:date><dc:format xmlns:dc=\"http://purl.org/dc/elements/1.1/\">image/jpeg</dc:format><dc:publisher xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Χρυσαλλίς</dc:publisher><dc:language xmlns:dc=\"http://purl.org/dc/elements/1.1/\">gr</dc:language><dc:relation xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Χρυσαλλίς; Vol 1, No 14 (1863); σελ. 431-432</dc:relation><dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Αρχαιολογικά</dc:title><dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Articles</dc:type><edm:europeanaProxy>false</edm:europeanaProxy><ore:proxyFor rdf:resource=\"http://data.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><ore:proxyIn rdf:resource=\"http://data.europeana.eu/aggregation/provider/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:type>TEXT</edm:type></ore:Proxy><ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/proxy/europeana/00501/330511407E2E987232571B5CCE38F689018984C8\"><edm:europeanaProxy>true</edm:europeanaProxy><ore:proxyFor rdf:resource=\"http://data.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><ore:proxyIn rdf:resource=\"http://data.europeana.eu/aggregation/europeana/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:type>TEXT</edm:type></ore:Proxy><edm:EuropeanaAggregation rdf:about=\"http://data.europeana.eu/aggregation/europeana/00501/330511407E2E987232571B5CCE38F689018984C8\"><edm:aggregatedCHO rdf:resource=\"http://data.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:collectionName>00501_L_GR_UniPatras_kosmopolis_dc</edm:collectionName><edm:country>Greece</edm:country><edm:preview rdf:resource=\"http://europeanastatic.eu/api/image?uri=http://xantho.lis.upatras.gr/test2.php?art=2994&amp;size=LARGE&amp;type=TEXT\"/><edm:landingPage rdf:resource=\"http://europeana.eu/portal/record/00501/330511407E2E987232571B5CCE38F689018984C8.html\"/><edm:language>el</edm:language></edm:EuropeanaAggregation></rdf:RDF></xmlData></GetRecord></OAI-PMH>";

    private static final String LIST_IDENTIFIERS_RESPONSE = "<OAI-PMH><responseDate>2018-03-20T12:06:57Z</responseDate><request verb=\"ListIdentifiers\" metadataPrefix=\"edm\" set=\"2026011_Ag_EU_DCA_Kultura.hr\">\"https://oai.europeana.eu/oai\"</request><ListIdentifiers><header><identifier>/2026011/_MMSU_1029</identifier><datestamp>2017-08-04T05:19:45Z</datestamp><setSpec>2026011_Ag_EU_DCA_Kultura.hr</setSpec></header><header><identifier>/2026011/_MMSU_1031</identifier><datestamp>2017-08-04T05:19:45Z</datestamp><setSpec>2026011_Ag_EU_DCA_Kultura.hr</setSpec></header><header><identifier>/2026011/_MMSU_1032</identifier><datestamp>2017-08-04T05:19:45Z</datestamp><setSpec>2026011_Ag_EU_DCA_Kultura.hr</setSpec></header><resumptionToken completeListSize=\"730\" expirationDate=\"2018-03-21T12:06:57Z\" cursor=\"0\">ZWRtX2RhdGFzZXROYW1lOiIyMDI2MDExX0FnX0VVX0RDQV9LdWx0dXJhLmhyIl9fXzE1MjE2MzQwMTcxNjZfX18wX19fQW9KMnRlM3IxZDBDTXk4eU1ESTJNREV4TDE5TlRWTlZYekl5TWprPQ==</resumptionToken></ListIdentifiers></OAI-PMH>";

    private static final String LIST_IDENTIFIERS_RESPONSE_WITH_TOKEN = "<OAI-PMH><responseDate>2018-03-30T08:18:52Z</responseDate><request verb=\"ListIdentifiers\" resumptionToken=\"ZWRtX2RhdGFzZXROYW1lOiIyMDQ4NDMyX0FnX0RFX0REQl9CSU5FXzk5OTAwNjc4Il9fXzE1MjI0ODM3NzI5MDhfX18wX19fQW9KMnI4N2oxdDBDUHc4dk1qQTBPRFF6TWk5cGRHVnRYMFZJTlZKUFJrNVlRalJJVDFOWldqUkJVazgyUjBSQ1NrVkhUMW96V1VnMQ==\">\"https://oai.europeana.eu/oai\"</request><ListIdentifiers><header><identifier>/2048432/item_EXXZUCJ6AHWDNHR2T7LISCVGGK5ZUGAN</identifier><datestamp>2017-08-04T14:03:00Z</datestamp><setSpec>2048432_Ag_DE_DDB_BINE_99900678</setSpec></header></ListIdentifiers></OAI-PMH>";

    private static final String LIST_IDENTIFIERS_TOKEN = "ZWRtX2RhdGFzZXROYW1lOiIyMDQ4NDMyX0FnX0RFX0REQl9CSU5FXzk5OTAwNjc4Il9fXzE1MjI0ODM3NzI5MDhfX18wX19fQW9KMnI4N2oxdDBDUHc4dk1qQTBPRFF6TWk5cGRHVnRYMFZJTlZKUFJrNVlRalJJVDFOWldqUkJVazgyUjBSQ1NrVkhUMW96V1VnMQ==";

    private static final String LIST_IDENTIFIERS_CORRUPTED_TOKEN = "ZWRtX19fQW9KMnI4N2oxdDBDUHc4dk1qQTBPRFF6TWk5cGRHVnRYMFZJTlZKUFJrNVlRaljBSQ1NrVkhUMW96V1VnMQ==";

    private MockMvc mockMvc;

    @Mock
    private OaiPmhService ops;

    @InjectMocks
    private VerbController verbController;

    /**
     * Loads the entire webapplication as mock server
     */
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(verbController).build();
        assertThat(this.mockMvc).isNotNull();
    }

    @Test
    public void testIdentify() throws Exception {
        given(ops.getIdentify(any(IdentifyRequest.class))).willReturn(IDENTIFY_RESPONSE);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=Identify").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetRecord() throws Exception {
        given(ops.getRecord(any(GetRecordRequest.class))).willReturn(RECORD_RESPONSE);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=GetRecord&metadataPrefix=edm&identifier=90402/BK_1978_399").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testListIdentifiersWithResumptionToken() throws Exception {
        given(ops.listIdentifiersWithToken(any(ListIdentifiersRequest.class))).willReturn(LIST_IDENTIFIERS_RESPONSE_WITH_TOKEN);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&resumptionToken=" + LIST_IDENTIFIERS_TOKEN).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testListIdentifiersWithCorruptedResumptionToken() throws Exception {
        given(ops.listIdentifiersWithToken(any(ListIdentifiersRequest.class))).willCallRealMethod();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&resumptionToken=" + LIST_IDENTIFIERS_CORRUPTED_TOKEN).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiers() throws Exception {
        given(ops.listIdentifiers(any(ListIdentifiersRequest.class))).willReturn(LIST_IDENTIFIERS_RESPONSE);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set=2026011_Ag_EU_DCA_Kultura.hr&from=2017-02-02T01:03:00Z&until=2017-03-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testInvalidVerb() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=XXX").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithEmptySet() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithMultipleSets() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set=ABC&set=XYZ").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithEmptyFrom() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithMultipleFroms() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2017-02-02T01:03:00Z&from=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithEmptyUntil() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&until=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithMultipleUntils() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&until=2017-02-02T01:03:00Z&until=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithIncorrectFrom() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2017.02.02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithIncorrectUntil() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&until=2017.02.02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithIncorrectPeriod() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2018-02-02T01:03:00Z&until=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithIncorrectParameterName() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2017-02-02T01:03:00Z&to=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
