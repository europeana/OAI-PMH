package eu.europeana.oaipmh.web;

import eu.europeana.oaipmh.model.request.*;
import eu.europeana.oaipmh.service.OaiPmhService;
import eu.europeana.oaipmh.service.exception.GlobalExceptionHandler;
import org.apache.commons.io.Charsets;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test the application's controller
 *
 * @author Patrick Ehlert
 * Created on 28-02-2018
 *
 * Srishti - MockMVC doesn't handle Charset UTF-8 anymore.
 * Now that charset is not the objective of the test Spring Boot provides more
 * flexible assertion of the content-type using contentTypeCompatibleWith()
 * We can improvide this later with Junit5
 */
@RunWith(MockitoJUnitRunner.class)
public class VerbControllerTest {

    private static final String IDENTIFY_RESPONSE = "<OAI-PMH><responseDate>2018-03-16T10:10:32Z</responseDate><request verb=\"Identify\">\"https://oai.europeana.eu/oai\"</request><Identify><repositoryName>Europeana Repository</repositoryName><earliestDateStamp>2013-02-15T13:04:50Z</earliestDateStamp><deletedRecord>no</deletedRecord><adminEmail>api@europeana.eu</adminEmail></Identify></OAI-PMH>";

    private static final String RECORD_RESPONSE = "<OAI-PMH><responseDate>2018-03-14T13:37:31Z</responseDate><request verb=\"GetRecord\">\"https://oai.europeana.eu/oai\"</request><GetRecord><xmlData><?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"><edm:ProvidedCHO rdf:about=\"http://data.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:WebResource rdf:about=\"http://xantho.lis.upatras.gr/kosmopolis/index.php/xrysallis/article/view/2994\"/><edm:WebResource rdf:about=\"http://xantho.lis.upatras.gr/test2.php?art=2994\"/><ore:Aggregation xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/aggregation/provider/00501/330511407E2E987232571B5CCE38F689018984C8\"><edm:aggregatedCHO rdf:resource=\"http://data.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:dataProvider>University of Patras/Library and Information Center</edm:dataProvider><edm:isShownAt rdf:resource=\"http://xantho.lis.upatras.gr/kosmopolis/index.php/xrysallis/article/view/2994\"/><edm:isShownBy rdf:resource=\"http://xantho.lis.upatras.gr/test2.php?art=2994\"/><edm:object rdf:resource=\"http://xantho.lis.upatras.gr/test2.php?art=2994\"/><edm:provider>University of Patras/Library and Information Center</edm:provider><edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/></ore:Aggregation><ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/proxy/provider/00501/330511407E2E987232571B5CCE38F689018984C8\"><dc:creator xmlns:dc=\"http://purl.org/dc/elements/1.1/\">[Ανωνύμως]</dc:creator><dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\">[1970]</dc:date><dc:format xmlns:dc=\"http://purl.org/dc/elements/1.1/\">image/jpeg</dc:format><dc:publisher xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Χρυσαλλίς</dc:publisher><dc:language xmlns:dc=\"http://purl.org/dc/elements/1.1/\">gr</dc:language><dc:relation xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Χρυσαλλίς; Vol 1, No 14 (1863); σελ. 431-432</dc:relation><dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Αρχαιολογικά</dc:title><dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Articles</dc:type><edm:europeanaProxy>false</edm:europeanaProxy><ore:proxyFor rdf:resource=\"http://data.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><ore:proxyIn rdf:resource=\"http://data.europeana.eu/aggregation/provider/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:type>TEXT</edm:type></ore:Proxy><ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/proxy/europeana/00501/330511407E2E987232571B5CCE38F689018984C8\"><edm:europeanaProxy>true</edm:europeanaProxy><ore:proxyFor rdf:resource=\"http://data.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><ore:proxyIn rdf:resource=\"http://data.europeana.eu/aggregation/europeana/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:type>TEXT</edm:type></ore:Proxy><edm:EuropeanaAggregation rdf:about=\"http://data.europeana.eu/aggregation/europeana/00501/330511407E2E987232571B5CCE38F689018984C8\"><edm:aggregatedCHO rdf:resource=\"http://data.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:collectionName>00501_L_GR_UniPatras_kosmopolis_dc</edm:collectionName><edm:country>Greece</edm:country><edm:preview rdf:resource=\"http://europeanastatic.eu/api/image?uri=http://xantho.lis.upatras.gr/test2.php?art=2994&amp;size=LARGE&amp;type=TEXT\"/><edm:landingPage rdf:resource=\"https://www.europeana.eu/item/00501/330511407E2E987232571B5CCE38F689018984C8\"/><edm:language>el</edm:language></edm:EuropeanaAggregation></rdf:RDF></xmlData></GetRecord></OAI-PMH>";

    private static final String LIST_IDENTIFIERS_RESPONSE = "<OAI-PMH><responseDate>2018-03-20T12:06:57Z</responseDate><request verb=\"ListIdentifiers\" metadataPrefix=\"edm\" set=\"2026011\">\"https://oai.europeana.eu/oai\"</request><ListIdentifiers><header><identifier>/2026011/_MMSU_1029</identifier><datestamp>2017-08-04T05:19:45Z</datestamp><setSpec>2026011</setSpec></header><header><identifier>/2026011/_MMSU_1031</identifier><datestamp>2017-08-04T05:19:45Z</datestamp><setSpec>2026011</setSpec></header><header><identifier>/2026011/_MMSU_1032</identifier><datestamp>2017-08-04T05:19:45Z</datestamp><setSpec>2026011</setSpec></header><resumptionToken completeListSize=\"730\" expirationDate=\"2018-03-21T12:06:57Z\" cursor=\"0\">ZWRtX2RhdGFzZXROYW1lOiIyMDI2MDExX0FnX0VVX0RDQV9LdWx0dXJhLmhyIl9fXzE1MjE2MzQwMTcxNjZfX18wX19fQW9KMnRlM3IxZDBDTXk4eU1ESTJNREV4TDE5TlRWTlZYekl5TWprPQ==</resumptionToken></ListIdentifiers></OAI-PMH>";

    private static final String LIST_IDENTIFIERS_RESPONSE_WITH_TOKEN = "<OAI-PMH><responseDate>2018-03-30T08:18:52Z</responseDate><request verb=\"ListIdentifiers\" resumptionToken=\"ZWRtX2RhdGFzZXROYW1lOiIyMDQ4NDMyX0FnX0RFX0REQl9CSU5FXzk5OTAwNjc4Il9fXzE1MjI0ODM3NzI5MDhfX18wX19fQW9KMnI4N2oxdDBDUHc4dk1qQTBPRFF6TWk5cGRHVnRYMFZJTlZKUFJrNVlRalJJVDFOWldqUkJVazgyUjBSQ1NrVkhUMW96V1VnMQ==\">\"https://oai.europeana.eu/oai\"</request><ListIdentifiers><header><identifier>/2048432/item_EXXZUCJ6AHWDNHR2T7LISCVGGK5ZUGAN</identifier><datestamp>2017-08-04T14:03:00Z</datestamp><setSpec>2048432</setSpec></header></ListIdentifiers></OAI-PMH>";

    private static final String LIST_IDENTIFIERS_TOKEN = "ZWRtX2RhdGFzZXROYW1lOiIyMDQ4NDMyX0FnX0RFX0REQl9CSU5FXzk5OTAwNjc4Il9fXzE1MjI0ODM3NzI5MDhfX18wX19fQW9KMnI4N2oxdDBDUHc4dk1qQTBPRFF6TWk5cGRHVnRYMFZJTlZKUFJrNVlRalJJVDFOWldqUkJVazgyUjBSQ1NrVkhUMW96V1VnMQ==";

    private static final String LIST_IDENTIFIERS_CORRUPTED_TOKEN = "ZWRtX19fQW9KMnI4N2oxdDBDUHc4dk1qQTBPRFF6TWk5cGRHVnRYMFZJTlZKUFJrNVlRaljBSQ1NrVkhUMW96V1VnMQ==";

    private static final String LIST_METADATA_FORMATS_RESPONSE = "<OAI-PMH xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\"><responseDate>2018-05-10T08:33:02Z</responseDate><request verb=\"ListMetadataFormats\">https://oai-pmh.europeana.eu/oai</request><ListMetadataFormats><metadataFormat><metadataPrefix>edm</metadataPrefix><schema>http://www.europeana.eu/schemas/edm/EDM.xsd</schema><metadataNamespace>http://www.europeana.eu/schemas/edm/</metadataNamespace></metadataFormat></ListMetadataFormats></OAI-PMH>";

    private static final String LIST_RECORDS_RESPONSE = "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\"><responseDate>2018-05-28T08:46:00Z</responseDate><request verb=\"ListRecords\" metadataPrefix=\"edm\" set=\"2048378\">https://oai-pmh.europeana.eu/oai</request><ListRecords><record><header><identifier>http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2</identifier><datestamp>2017-11-16T10:37:32Z</datestamp><setSpec>2048378</setSpec></header><metadata><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"><edm:ProvidedCHO rdf:about=\"http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"/><edm:WebResource rdf:about=\"https://www.flickr.com/photos/archivesportaleurope/38238893326/in/album-72157689082981984/\"><dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\">HR-HDA-907-1</dc:description><edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/></edm:WebResource><edm:WebResource rdf:about=\"http://www.archivesportaleuropefoundation.eu/images/thumbs/HR-HDA-907-1_thumb.jpg\"/><edm:WebResource rdf:about=\"http://www.archivesportaleuropefoundation.eu/images/scans/HR-HDA-907-1.jpg\"/><edm:TimeSpan rdf:about=\"http://semium.org/time/1913\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">1913</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx_1_third\"></dcterms:isPartOf><edm:begin>Wed Jan 01 00:19:32 CET 1913</edm:begin><edm:end>Wed Dec 31 00:19:32 CET 1913</edm:end></edm:TimeSpan><edm:TimeSpan rdf:about=\"http://semium.org/time/AD2xxx\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Second millenium AD</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Second millenium AD, years 1001-2000</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">2e millénaire après J.-C.</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/ChronologicalPeriod\"></dcterms:isPartOf></edm:TimeSpan><edm:TimeSpan rdf:about=\"http://semium.org/time/19xx\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ru\">20й век</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">20..</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">20??</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">20e</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">20-th</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">20th</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">20th century</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">20e siècle</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"nl\">20de eeuw</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/AD2xxx\"></dcterms:isPartOf><edm:begin>Tue Jan 01 00:19:32 CET 1901</edm:begin><edm:end>Sun Dec 31 01:00:00 CET 2000</edm:end></edm:TimeSpan><edm:TimeSpan rdf:about=\"http://semium.org/time/19xx_1_third\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ru\">Начало 20-го века</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Early 20th century</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx\"></dcterms:isPartOf><edm:begin>Tue Jan 01 00:19:32 CET 1901</edm:begin><edm:end>Sun Dec 31 00:19:32 CET 1933</edm:end></edm:TimeSpan><ore:Aggregation xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/aggregation/provider/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"><edm:aggregatedCHO rdf:resource=\"http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"/><edm:dataProvider>Hrvatski državni arhiv</edm:dataProvider><edm:isShownAt rdf:resource=\"https://www.flickr.com/photos/archivesportaleurope/38238893326/in/album-72157689082981984/\"/><edm:isShownBy rdf:resource=\"http://www.archivesportaleuropefoundation.eu/images/scans/HR-HDA-907-1.jpg\"/><edm:object rdf:resource=\"http://www.archivesportaleuropefoundation.eu/images/thumbs/HR-HDA-907-1_thumb.jpg\"/><edm:provider>Archives Portal Europe</edm:provider><edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/></ore:Aggregation><ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/proxy/provider/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"><dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\">1913</dc:date><dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\">HR-HDA-907 - 2.2</dc:identifier><dc:language xmlns:dc=\"http://purl.org/dc/elements/1.1/\">hr</dc:language><dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Democracy</dc:subject><dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Long live Dr Gjuro Šurmin</dc:title><dcterms:created xmlns:dcterms=\"http://purl.org/dc/terms/\">1913</dcterms:created><edm:europeanaProxy>false</edm:europeanaProxy><ore:proxyFor rdf:resource=\"http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"/><ore:proxyIn rdf:resource=\"http://data.europeana.eu/aggregation/provider/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"/><edm:type>TEXT</edm:type></ore:Proxy><ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/proxy/europeana/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"><dcterms:created xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/1913\"></dcterms:created><dcterms:created xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx_1_third\"></dcterms:created><dcterms:temporal xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx\"></dcterms:temporal><edm:europeanaProxy>true</edm:europeanaProxy><edm:year>1913</edm:year><ore:proxyFor rdf:resource=\"http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"/><ore:proxyIn rdf:resource=\"http://data.europeana.eu/aggregation/europeana/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"/><edm:type>TEXT</edm:type></ore:Proxy><edm:EuropeanaAggregation rdf:about=\"http://data.europeana.eu/aggregation/europeana/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"><edm:aggregatedCHO rdf:resource=\"http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"/><edm:datasetName>2048378_Ag_EU_ApeX_Croatia_HDA</edm:datasetName><edm:country>Croatia</edm:country><edm:preview rdf:resource=\"http://www.archivesportaleuropefoundation.eu/images/thumbs/HR-HDA-907-1_thumb.jpg\"/><edm:landingPage rdf:resource=\"https://www.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_2\"/><edm:language>hr</edm:language><edm:completeness>4</edm:completeness></edm:EuropeanaAggregation></rdf:RDF></metadata></record><record><header><identifier>http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21</identifier><datestamp>2017-11-16T10:37:32Z</datestamp><setSpec>2048378</setSpec></header><metadata><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"><edm:ProvidedCHO rdf:about=\"http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"/><edm:WebResource rdf:about=\"https://www.flickr.com/photos/archivesportaleurope/26518208489/in/album-72157689082981984/\"><dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\">HR-HDA-907-2.21_1</dc:description><edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/></edm:WebResource><edm:WebResource rdf:about=\"https://www.flickr.com/photos/archivesportaleurope/38293726401/in/album-72157689082981984/\"><dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\">HR-HDA-907-2.21_2</dc:description><edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/></edm:WebResource><edm:WebResource rdf:about=\"https://www.flickr.com/photos/archivesportaleurope/26518208209/in/album-72157689082981984/\"><dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\">HR-HDA-907-2.21_3</dc:description><edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/></edm:WebResource><edm:WebResource rdf:about=\"http://www.archivesportaleuropefoundation.eu/images/thumbs/HR-HDA-907-2_thumb.jpg\"/><edm:WebResource rdf:about=\"http://www.archivesportaleuropefoundation.eu/images/scans/HR-HDA-907-2.jpg\"/><edm:WebResource rdf:about=\"http://www.archivesportaleuropefoundation.eu/images/scans/HR-HDA-907-3.jpg\"/><edm:WebResource rdf:about=\"http://www.archivesportaleuropefoundation.eu/images/scans/HR-HDA-907-4.jpg\"/><edm:TimeSpan rdf:about=\"http://semium.org/time/1909\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">1909</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx_1_third\"></dcterms:isPartOf><edm:begin>Fri Jan 01 00:19:32 CET 1909</edm:begin><edm:end>Fri Dec 31 00:19:32 CET 1909</edm:end></edm:TimeSpan><edm:TimeSpan rdf:about=\"http://semium.org/time/AD2xxx\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Second millenium AD</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Second millenium AD, years 1001-2000</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">2e millénaire après J.-C.</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/ChronologicalPeriod\"></dcterms:isPartOf></edm:TimeSpan><edm:TimeSpan rdf:about=\"http://semium.org/time/19xx\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ru\">20й век</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">20..</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">20??</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">20e</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">20-th</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">20th</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">20th century</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">20e siècle</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"nl\">20de eeuw</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/AD2xxx\"></dcterms:isPartOf><edm:begin>Tue Jan 01 00:19:32 CET 1901</edm:begin><edm:end>Sun Dec 31 01:00:00 CET 2000</edm:end></edm:TimeSpan><edm:TimeSpan rdf:about=\"http://semium.org/time/19xx_1_third\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ru\">Начало 20-го века</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Early 20th century</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx\"></dcterms:isPartOf><edm:begin>Tue Jan 01 00:19:32 CET 1901</edm:begin><edm:end>Sun Dec 31 00:19:32 CET 1933</edm:end></edm:TimeSpan><ore:Aggregation xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/aggregation/provider/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"><edm:aggregatedCHO rdf:resource=\"http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"/><edm:dataProvider>Hrvatski državni arhiv</edm:dataProvider><edm:hasView rdf:resource=\"http://www.archivesportaleuropefoundation.eu/images/scans/HR-HDA-907-3.jpg\"/><edm:hasView rdf:resource=\"http://www.archivesportaleuropefoundation.eu/images/scans/HR-HDA-907-4.jpg\"/><edm:isShownAt rdf:resource=\"https://www.flickr.com/photos/archivesportaleurope/26518208489/in/album-72157689082981984/\"/><edm:isShownBy rdf:resource=\"http://www.archivesportaleuropefoundation.eu/images/scans/HR-HDA-907-2.jpg\"/><edm:object rdf:resource=\"http://www.archivesportaleuropefoundation.eu/images/thumbs/HR-HDA-907-2_thumb.jpg\"/><edm:provider>Archives Portal Europe</edm:provider><edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/mark/1.0/\"/></ore:Aggregation><ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/proxy/provider/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"><dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\">1909</dc:date><dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\">HR-HDA-907 - 2.21</dc:identifier><dc:language xmlns:dc=\"http://purl.org/dc/elements/1.1/\">hr</dc:language><dc:subject xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Democracy</dc:subject><dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Who will you vote for?</dc:title><dcterms:created xmlns:dcterms=\"http://purl.org/dc/terms/\">1909</dcterms:created><edm:europeanaProxy>false</edm:europeanaProxy><ore:proxyFor rdf:resource=\"http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"/><ore:proxyIn rdf:resource=\"http://data.europeana.eu/aggregation/provider/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"/><edm:type>TEXT</edm:type></ore:Proxy><ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/proxy/europeana/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"><dcterms:created xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/1909\"></dcterms:created><dcterms:created xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx_1_third\"></dcterms:created><dcterms:temporal xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx\"></dcterms:temporal><edm:europeanaProxy>true</edm:europeanaProxy><edm:year>1909</edm:year><ore:proxyFor rdf:resource=\"http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"/><ore:proxyIn rdf:resource=\"http://data.europeana.eu/aggregation/europeana/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"/><edm:type>TEXT</edm:type></ore:Proxy><edm:EuropeanaAggregation rdf:about=\"http://data.europeana.eu/aggregation/europeana/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"><edm:aggregatedCHO rdf:resource=\"http://data.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"/><edm:datasetName>2048378_Ag_EU_ApeX_Croatia_HDA</edm:datasetName><edm:country>Croatia</edm:country><edm:preview rdf:resource=\"http://www.archivesportaleuropefoundation.eu/images/thumbs/HR-HDA-907-2_thumb.jpg\"/><edm:landingPage rdf:resource=\"https://www.europeana.eu/item/2048378/providedCHO_HR_HDA_907_HR_HDA_907___2_21\"/><edm:language>hr</edm:language><edm:completeness>5</edm:completeness></edm:EuropeanaAggregation></rdf:RDF></metadata></record></ListRecords></OAI-PMH>";

    private static final String LIST_RECORDS_RESPONSE_WITH_TOKEN = "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\"><responseDate>2018-05-28T08:52:14Z</responseDate><request verb=\"ListRecords\" resumptionToken=\"fHw5MjAwNTA5fGVkbXwxNTI3NTgzNjYwMjQ2fDUxfDB8QW9KOW9vV2h4TjBDUHdjdk9USXdNRFV3T1M5dVlYTnNiM1p1WlY5emJHbHJaVjlqWlhScGJtcGxNRE0yWDJwd1p3PT0=\">https://oai-pmh.europeana.eu/oai</request><ListRecords><record><header><identifier>http://data.europeana.eu/item/9200509/naslovne_slike_cetinje026_jpg</identifier><datestamp>2017-07-28T09:25:49Z</datestamp><setSpec>9200509</setSpec></header><metadata><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"><edm:ProvidedCHO rdf:about=\"http://data.europeana.eu/item/9200509/naslovne_slike_cetinje026_jpg\"/><edm:WebResource rdf:about=\"http://www.dlib.me/jedinica_prikaz.php?cb=24796688\"/><edm:WebResource rdf:about=\"http://www.dlib.me/naslovne_slike/cetinje026.jpg\"><ebucore:hasMimeType xmlns:ebucore=\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\">image/jpeg</ebucore:hasMimeType><ebucore:fileByteSize xmlns:ebucore=\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\" rdf:datatype=\"http://www.w3.org/2001/XMLSchema#long\">256066</ebucore:fileByteSize><ebucore:width xmlns:ebucore=\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\" rdf:datatype=\"http://www.w3.org/2001/XMLSchema#integer\">1863</ebucore:width><ebucore:height xmlns:ebucore=\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\" rdf:datatype=\"http://www.w3.org/2001/XMLSchema#integer\">1282</ebucore:height><edm:hasColorSpace>sRGB</edm:hasColorSpace><edm:componentColor rdf:datatype=\"http://www.w3.org/2001/XMLSchema#hexBinary\">#2F4F4F</edm:componentColor><edm:componentColor rdf:datatype=\"http://www.w3.org/2001/XMLSchema#hexBinary\">#C0C0C0</edm:componentColor><edm:componentColor rdf:datatype=\"http://www.w3.org/2001/XMLSchema#hexBinary\">#F5F5F5</edm:componentColor><edm:componentColor rdf:datatype=\"http://www.w3.org/2001/XMLSchema#hexBinary\">#000000</edm:componentColor><edm:componentColor rdf:datatype=\"http://www.w3.org/2001/XMLSchema#hexBinary\">#696969</edm:componentColor><edm:componentColor rdf:datatype=\"http://www.w3.org/2001/XMLSchema#hexBinary\">#556B2F</edm:componentColor><ebucore:orientation xmlns:ebucore=\"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\" rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">landscape</ebucore:orientation></edm:WebResource><edm:Place rdf:about=\"http://sws.geonames.org/3202641/\"><wgs84:lat xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">42.39063</wgs84:lat><wgs84:long xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">18.91417</wgs84:long><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ru\">Цетине</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">Cetińe</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Cetinje</skos:prefLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"de\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fi\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">Cettinge</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"pt\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"bg\">Цетине</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"lt\">Cetinė</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"hr\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"bs\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"hy\">Ցետինե</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"uk\">Цетинє</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sq\">Cetina</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"mk\">Цетиње</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sr\">Цетиње</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"vep\">Cetine</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sv\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"os\">Цетине</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ko\">체티네</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"eo\">Cetinjo</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"it\">Cettigne</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"es\">Cetiña</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"zh\">采蒂涅</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"et\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"cs\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ar\">ستنيي</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"cu\">Цєтиниѥ</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"th\">เซตีเนีย</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ja\">ツェティニェ</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"hbs\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fa\">ستینیه</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"pl\">Cetynia</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"he\">צטיניה</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"da\">Cetinje</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"nl\">Cetinje</skos:altLabel><skos:note xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">http://ru.wikipedia.org/wiki/%D0%A6%D0%B5%D1%82%D0%B8%D0%BD%D1%8C%D0%B5</skos:note></edm:Place><edm:Place rdf:about=\"http://data.europeana.eu/place/base/103\"><wgs84:lat xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">42.75</wgs84:lat><wgs84:long xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">19.25</wgs84:long><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"hi\">मोंटेनेग्रो</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ps\">مانتېنېگرو</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"pt\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"hr\">Crna Gora</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ht\">Montenegwo</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"hu\">Montenegró</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"yi\">מאנטענעגרא</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"hy\">Չեռնոգորիա</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"yo\">Montenẹ́grò</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"id\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"qu\">Yanaurqu</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"af\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"is\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"is\">Svartfjallaland</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"it\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"am\">ሞንተኔግሮ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"am\">ሞንቴኔግሮ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"zh\">蒙特內哥羅</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"zh\">黑山共和国</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ar\">الجبل الأسود</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"av\">ЧІегІермегІер</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ja\">モンテネグロ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"az\">Monteneqro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"az\">Çernoqoriya</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"zu\">IMontenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"zu\">i-Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"rm\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ro\">Muntenegru</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ba\">Черногория</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"be\">Чарнагорыя</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ru\">Черногория</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"rw\">Montenegoro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"bg\">Черна гора</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"bh\">मोंटीनीग्रो</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"jv\">Montenégro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"bn\">মন্টিনিগ্রো</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"bo\">མོན་ཊེནིག་རོ།</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"br\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"bs\">Crna Gora</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"se\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"si\">මොන්ටිනිග්\u200Dරෝ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"si\">මොන්ඩිනීග්\u200Dරෝ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ka\">მონტენეგრო</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ka\">ჩერნოგორია</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sk\">Čierna Hora</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sl\">Črna gora</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"kg\">Monte Negro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sq\">Mali i Zi</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ca\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sr\">Crna Gora</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sr\">Republika Crna Gora</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sr\">Црна Гора</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ss\">IMonthenekho</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"kk\">Черногория</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"kl\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"su\">Monténégro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ce\">Ӏаьржаламанчоь</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sv\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"kn\">ಮಾಂಟೆನೆಗ್ರೊ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"kn\">ಮೊಂಟೆನೆಗ್ರೋ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sw\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ko\">몬테네그로</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ku\">مۆنتینیگرۆ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"kv\">Черногория</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"co\">Montenegru</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ta\">மான்டேனெக்ரோ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ta\">மொண்டெனேகுரோ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ky\">Монтенегро</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"cs\">Černá Hora</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"te\">మోంటేనేగ్రో</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"cu\">Чрьна Гора</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"tg\">Монтенегро</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"cv\">Черногори</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"th\">ประเทศมอนเตเนโกร</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"th\">มอนเตเนโกร</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"la\">Mons Niger</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"cy\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"tk\">Çernogoriýa</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"to\">Monitenikalo</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"da\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"tr\">Karadağ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"tt\">Монтенегро</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"de\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"lt\">Juodkalnija</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"lv\">Melnkalne</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ug\">مونته\u200Cنەگرو</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"dv\">މޮންޓެނީގުރޯ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"uk\">Чорногорія</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ur\">مونٹینیگرو</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ur\">مونٹے نیگرو</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"mk\">Црна Гора</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ml\">മൊണ്ടിനെഗ്രോ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ml\">മോണ്ടേനേഗ്രോ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ee\">Montenegro nutome</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"uz\">Chernogoriya</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"mr\">माँटेनिग्रो</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"mr\">मोंटेनेग्रो</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ms\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"el\">Μαυροβούνιο</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Republic of Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Socialist Republic of Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"my\">မွန်တီနိဂရိုး</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"my\">မွန်တီနီဂရိုးနိုင်ငံ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"es\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"et\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"eu\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"vi\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"nb\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ne\">मोन्टेनेग्रो</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"vo\">Montenegrän</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fa\">مونته\u200Cنگرو</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"nl\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"nn\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fi\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"nv\">Dziłizhin Bikéyah</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fo\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">Monténégro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">République du Monténégro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ga\">Montainéagró</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"gd\">Am Monadh Neagrach</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"om\">Moonteneegroo</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"or\">ମଣ୍ଟେଗ୍ରୋ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"or\">ମୋଣ୍ଟେନେଗ୍ରୋ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"os\">Черногори</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"gl\">Montenegro</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"gu\">મૉન્ટેંનેગ્રો</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"gu\">મોન્ટેનીગ્રો</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"pa\">ਮੋਂਟੇਨੇਗਰੋ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"pl\">Czarnogóra</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"he\">מונטנגרו</skos:prefLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">ME</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">People’s Republic of Montenegro</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">Republika Crna Gora</skos:altLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">Socialist Republic of Montenegro</skos:altLabel><owl:sameAs xmlns:owl=\"http://www.w3.org/2002/07/owl#\" rdf:resource=\"http://sws.geonames.org/3194884/\"/></edm:Place><edm:Place rdf:about=\"http://data.europeana.eu/place/base/142858\"><wgs84:lat xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">42.39063</wgs84:lat><wgs84:long xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">18.91417</wgs84:long><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"de\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fi\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ru\">Цетине</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"pt\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"bg\">Цетине</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"lt\">Cetinė</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"hr\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"bs\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"hy\">Ցետինե</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"uk\">Цетинє</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sq\">Cetina</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"mk\">Цетиње</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sr\">Цетиње</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sv\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"os\">Цетине</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ko\">체티네</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"eo\">Cetinjo</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"it\">Cettigne</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"es\">Cetiña</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"zh\">采蒂涅</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"et\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"cs\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ar\">ستنيي</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"cu\">Цєтиниѥ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"th\">เซตีเนีย</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ja\">ツェティニェ</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fa\">ستینیه</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"pl\">Cetynia</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"da\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"he\">צטיניה</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"nl\">Cetinje</skos:prefLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">Cettinge</skos:altLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://data.europeana.eu/place/base/103\"></dcterms:isPartOf><owl:sameAs xmlns:owl=\"http://www.w3.org/2002/07/owl#\" rdf:resource=\"http://sws.geonames.org/3202641/\"/></edm:Place><edm:Place rdf:about=\"http://data.europeana.eu/place/base/142857\"><wgs84:lat xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">42.4</wgs84:lat><wgs84:long xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\">18.91972</wgs84:long><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sr\">Prijestonica Cetinje</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sr\">Општина Цетиње</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sr\">Пријестоница Цетиње</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"sr\">Цетиње</skos:prefLabel><skos:altLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">Cetinje</skos:altLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://data.europeana.eu/place/base/103\"></dcterms:isPartOf><owl:sameAs xmlns:owl=\"http://www.w3.org/2002/07/owl#\" rdf:resource=\"http://sws.geonames.org/3202640/\"/></edm:Place><edm:TimeSpan rdf:about=\"http://semium.org/time/19xx_1_half\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ru\">Первая половниа 20-го века</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">First half of the 20th century</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">20e (début)</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx\"></dcterms:isPartOf><edm:begin>Tue Jan 01 00:19:32 CET 1901</edm:begin><edm:end>Mon Dec 31 01:00:00 CET 1951</edm:end></edm:TimeSpan><edm:TimeSpan rdf:about=\"http://semium.org/time/19xx_2_quarter\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ru\">2-я четверть 20-го века</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">2 quarter of the 20th century</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">2e quart 20e siècle</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx_1_half\"></dcterms:isPartOf><edm:begin>Fri Jan 01 00:19:32 CET 1926</edm:begin><edm:end>Sun Dec 31 01:00:00 CET 1950</edm:end></edm:TimeSpan><edm:TimeSpan rdf:about=\"http://semium.org/time/19xx\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"ru\">20й век</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">20..</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">20??</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">20e</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">20-th</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">20th</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">20th century</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">20e siècle</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"nl\">20de eeuw</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/AD2xxx\"></dcterms:isPartOf><edm:begin>Tue Jan 01 00:19:32 CET 1901</edm:begin><edm:end>Sun Dec 31 01:00:00 CET 2000</edm:end></edm:TimeSpan><edm:TimeSpan rdf:about=\"http://semium.org/time/1928\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">1928</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx_2_quarter\"></dcterms:isPartOf><edm:begin>Sun Jan 01 00:19:32 CET 1928</edm:begin><edm:end>Mon Dec 31 00:19:32 CET 1928</edm:end></edm:TimeSpan><edm:TimeSpan rdf:about=\"http://semium.org/time/AD2xxx\"><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Second millenium AD</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"en\">Second millenium AD, years 1001-2000</skos:prefLabel><skos:prefLabel xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xml:lang=\"fr\">2e millénaire après J.-C.</skos:prefLabel><dcterms:isPartOf xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/ChronologicalPeriod\"></dcterms:isPartOf></edm:TimeSpan><ore:Aggregation xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/aggregation/provider/9200509/naslovne_slike_cetinje026_jpg\"><edm:aggregatedCHO rdf:resource=\"http://data.europeana.eu/item/9200509/naslovne_slike_cetinje026_jpg\"/><edm:dataProvider>National library of Montenegro \"Đurđe Crnojević\"</edm:dataProvider><edm:isShownAt rdf:resource=\"http://www.dlib.me/jedinica_prikaz.php?cb=24796688\"/><edm:isShownBy rdf:resource=\"http://www.dlib.me/naslovne_slike/cetinje026.jpg\"/><edm:provider>The European Library</edm:provider><edm:rights rdf:resource=\"http://creativecommons.org/publicdomain/zero/1.0/\"/></ore:Aggregation><ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/proxy/provider/9200509/naslovne_slike_cetinje026_jpg\"><dc:contributor xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"sr\">Д. Н. Бјеладиновић и брат (Котор) (lse)</dc:contributor><dc:coverage xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"sr\">Цетиње</dc:coverage><dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\">1928</dc:date><dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"sr\">Наслов и поднаслов преузети са аверсне стране разгледнице.</dc:description><dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"sr\">Податак о процијењеној години издања формиран на основу жига.</dc:description><dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"sr\">Поштанска марка краља Александра.</dc:description><dc:format xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"sr\">1 разгледница : ручно бојена ; 14 x 9 cm</dc:format><dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\">24796688</dc:identifier><dc:publisher xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"sr\">Котор : Д. Н. Бјеладиновић и брат</dc:publisher><dc:language xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"sr\">sr</dc:language><dc:rights xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Javno vlasništvo</dc:rights><dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"sr\">Цетиње : Доње Поље</dc:title><dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"en\">still image - art reproduction</dc:type><dc:type xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xml:lang=\"sr\">Сликовна грађа</dc:type><dcterms:issued xmlns:dcterms=\"http://purl.org/dc/terms/\" xml:lang=\"sr\">1928</dcterms:issued><dcterms:provenance xmlns:dcterms=\"http://purl.org/dc/terms/\" xml:lang=\"sr\">Kartografsko-geografska zbirka NBCG</dcterms:provenance><dcterms:spatial xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://sws.geonames.org/3202641/\"></dcterms:spatial><dcterms:temporal xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/1928\"></dcterms:temporal><edm:europeanaProxy>false</edm:europeanaProxy><ore:proxyFor rdf:resource=\"http://data.europeana.eu/item/9200509/naslovne_slike_cetinje026_jpg\"/><ore:proxyIn rdf:resource=\"http://data.europeana.eu/aggregation/provider/9200509/naslovne_slike_cetinje026_jpg\"/><edm:type>IMAGE</edm:type></ore:Proxy><ore:Proxy xmlns:ore=\"http://www.openarchives.org/ore/terms/\" rdf:about=\"http://data.europeana.eu/proxy/europeana/9200509/naslovne_slike_cetinje026_jpg\"><dc:coverage xmlns:dc=\"http://purl.org/dc/elements/1.1/\" rdf:resource=\"http://data.europeana.eu/place/base/103\"></dc:coverage><dc:coverage xmlns:dc=\"http://purl.org/dc/elements/1.1/\" rdf:resource=\"http://data.europeana.eu/place/base/142858\"></dc:coverage><dc:coverage xmlns:dc=\"http://purl.org/dc/elements/1.1/\" rdf:resource=\"http://data.europeana.eu/place/base/142857\"></dc:coverage><dc:date xmlns:dc=\"http://purl.org/dc/elements/1.1/\" rdf:resource=\"http://semium.org/time/19xx_1_half\"></dc:date><dcterms:issued xmlns:dcterms=\"http://purl.org/dc/terms/\" rdf:resource=\"http://semium.org/time/19xx\"></dcterms:issued><edm:europeanaProxy>true</edm:europeanaProxy><edm:year>1928</edm:year><ore:proxyFor rdf:resource=\"http://data.europeana.eu/item/9200509/naslovne_slike_cetinje026_jpg\"/><ore:proxyIn rdf:resource=\"http://data.europeana.eu/aggregation/europeana/9200509/naslovne_slike_cetinje026_jpg\"/><edm:type>IMAGE</edm:type></ore:Proxy><edm:EuropeanaAggregation rdf:about=\"http://data.europeana.eu/aggregation/europeana/9200509/naslovne_slike_cetinje026_jpg\"><edm:aggregatedCHO rdf:resource=\"http://data.europeana.eu/item/9200509/naslovne_slike_cetinje026_jpg\"/><edm:datasetName>9200509_NL_Postcards_Montenegro</edm:datasetName><edm:country>Montenegro</edm:country><edm:preview rdf:resource=\"http://www.dlib.me/naslovne_slike/cetinje026.jpg\"/><edm:landingPage rdf:resource=\"https://www.europeana.eu/item/9200509/naslovne_slike_cetinje026_jpg\"/><edm:language>sr</edm:language><edm:completeness>0</edm:completeness></edm:EuropeanaAggregation></rdf:RDF></metadata></record></ListRecords></OAI-PMH>";

    private static final String LIST_RECORDS_TOKEN = "fHw5MjAwNTA5fGVkbXwxNTI3NTgzNjYwMjQ2fDUxfDB8QW9KOW9vV2h4TjBDUHdjdk9USXdNRFV3T1M5dVlYTnNiM1p1WlY5emJHbHJaVjlqWlhScGJtcGxNRE0yWDJwd1p3PT0=";

    private static final String LIST_RECORDS_CORRUPTED_TOKEN = "ZWRtX19fQW9KMnI4N2oxdDBDUHc4dk1qQTBPRFF6TWk5cGRHVnRYMFZJTlZKUFJrNVlRaljBSQ1NrVkhUMW96V1VnMQ==";

    private static final String MEDIA_TYPE_TEXT_XML = "text/xml;charset=UTF-8";
    private static final String MEDIA_TYPE_APPLICATION_XML = "application/xml;charset=UTF-8";

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
        GlobalExceptionHandler globalExceptionHandler = spy(GlobalExceptionHandler.class);
        mockMvc = MockMvcBuilders.standaloneSetup(verbController).setControllerAdvice(globalExceptionHandler).build();
        assertThat(this.mockMvc).isNotNull();
    }

    @Test
    public void testIdentify() throws Exception {
        given(ops.getIdentify(any(IdentifyRequest.class))).willReturn(IDENTIFY_RESPONSE);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=Identify").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/oai?verb=Identify").accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));

        // if no headers - default to application/xml
        this.mockMvc.perform(MockMvcRequestBuilders.post("/oai?verb=Identify"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));
    }

    @Test
    public void testListMetadataFormats() throws Exception {
        given(ops.listMetadataFormats(any(ListMetadataFormatsRequest.class))).willReturn(LIST_METADATA_FORMATS_RESPONSE);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListMetadataFormats").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListMetadataFormats").accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListMetadataFormats"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));
    }

    @Test
    public void testListMetadataFormatsWrongArgument() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListMetadataFormats&resumptionToken=XXX").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListMetadataFormatsWrongMethod() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.put("/oai?verb=ListMetadataFormats").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testGetRecord() throws Exception {
        given(ops.getRecord(any(GetRecordRequest.class))).willReturn(RECORD_RESPONSE);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=GetRecord&metadataPrefix=edm&identifier=90402/BK_1978_399").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=GetRecord&metadataPrefix=edm&identifier=90402/BK_1978_399").accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=GetRecord&metadataPrefix=edm&identifier=90402/BK_1978_399"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));
    }

    @Test
    public void testListIdentifiersWithResumptionToken() throws Exception {
        given(ops.listIdentifiers(any(ListIdentifiersRequest.class))).willReturn(LIST_IDENTIFIERS_RESPONSE_WITH_TOKEN);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&resumptionToken=" + LIST_IDENTIFIERS_TOKEN).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&resumptionToken=" + LIST_IDENTIFIERS_TOKEN).accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&resumptionToken=" + LIST_IDENTIFIERS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));
    }

    @Test
    public void testListIdentifiersWithCorruptedResumptionToken() throws Exception {
        given(ops.listIdentifiers(any(ListIdentifiersRequest.class))).willCallRealMethod();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&resumptionToken=" + LIST_IDENTIFIERS_CORRUPTED_TOKEN).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiers() throws Exception {
        given(ops.listIdentifiers(any(ListIdentifiersRequest.class))).willReturn(LIST_IDENTIFIERS_RESPONSE);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set=2026011&from=2017-02-02T01:03:00Z&until=2017-03-02T01:03:00Z")
                        .accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set=2026011&from=2017-02-02T01:03:00Z&until=2017-03-02T01:03:00Z")
                        .accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));


        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set=2026011&from=2017-02-02T01:03:00Z&until=2017-03-02T01:03:00Z"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));
    }

    @Test
    public void testInvalidVerb() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=XXX").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithEmptySet() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithMultipleSets() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&set=123&set=1234").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithEmptyFrom() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithExclusiveParameters() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&resumptionToken=ABBB").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithExclusiveParametersForToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&resumptionToken=ABBB&set=123").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithMultipleFroms() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2017-02-02T01:03:00Z&from=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testListIdentifiersWithEmptyUntil() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&until=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithMultipleUntils() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&until=2017-02-02T01:03:00Z&until=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithIncorrectFrom() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2017.02.02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithIncorrectUntil() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&until=2017.02.02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithIncorrectPeriod() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2018-02-02T01:03:00Z&until=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListIdentifiersWithIncorrectParameterName() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListIdentifiers&metadataPrefix=edm&from=2017-02-02T01:03:00Z&to=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithResumptionToken() throws Exception {
        given(ops.listRecords(any(ListRecordsRequest.class))).willReturn(LIST_RECORDS_RESPONSE_WITH_TOKEN);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&resumptionToken=" + LIST_RECORDS_TOKEN).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&resumptionToken=" + LIST_RECORDS_TOKEN).accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&resumptionToken=" + LIST_RECORDS_TOKEN))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));
    }

    @Test
    public void testListRecordsWithCorruptedResumptionToken() throws Exception {
        given(ops.listRecords(any(ListRecordsRequest.class))).willCallRealMethod();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&resumptionToken=" + LIST_RECORDS_CORRUPTED_TOKEN).accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecords() throws Exception {
        given(ops.listRecords(any(ListRecordsRequest.class))).willReturn(LIST_RECORDS_RESPONSE);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&set=2026011&from=2017-02-02T01:03:00Z&until=2017-03-02T01:03:00Z")
                        .accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_XML));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&set=2026011&from=2017-02-02T01:03:00Z&until=2017-03-02T01:03:00Z")
                        .accept(MediaType.parseMediaType("application/xml")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&set=2026011&from=2017-02-02T01:03:00Z&until=2017-03-02T01:03:00Z"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_XML_VALUE));
    }

    @Test
    public void testListRecordsWithEmptySet() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&set=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithMultipleSets() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&set=123&set=345").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithEmptyFrom() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&from=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithExclusiveParameters() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&resumptionToken=ABBB").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithExclusiveParametersForToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&resumptionToken=ABBB&set=1232").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithMultipleFroms() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&from=2017-02-02T01:03:00Z&from=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithEmptyUntil() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&until=").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithMultipleUntils() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&until=2017-02-02T01:03:00Z&until=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithIncorrectFrom() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&from=2017.02.02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithIncorrectUntil() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&until=2017.02.02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithIncorrectPeriod() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&from=2018-02-02T01:03:00Z&until=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }

    @Test
    public void testListRecordsWithIncorrectParameterName() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/oai?verb=ListRecords&metadataPrefix=edm&from=2017-02-02T01:03:00Z&to=2017-02-02T01:03:00Z").accept(MediaType.parseMediaType("text/xml")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MEDIA_TYPE_TEXT_XML));
    }
}
