package eu.europeana.oaipmh;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.oaipmh.model.metadata.MetadataFormatsService;
import eu.europeana.oaipmh.service.*;
import eu.europeana.oaipmh.utils.MongoContainer;
import eu.europeana.oaipmh.utils.SolrContainer;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Abstract base class for integration tests.
 * This class configures test containers and provides common functionality and beans
 * for integration testing in the context of OAI-PMH services.
 *
 * The class utilizes the Spring Boot testing framework, as well as Testcontainers
 * for managing Docker containers during tests. It sets up both MongoDB and Solr
 * containers for use in integration tests and registers their configurations
 * in the application environment.
 */
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbstractIntegrationIT extends OaiPmhITConstants {

    private static final Logger logger = LogManager.getLogger(AbstractIntegrationIT.class);

    @Autowired
    protected RecordProvider recordProvider;

    @Autowired
    protected MetadataFormatsService metadataFormatsService;

    @Autowired
    protected IdentifyProvider identifyProvider;

    @Autowired
    protected SetsProvider setsProvider;

    @Autowired
    protected  IdentifierProvider identifierProvider;

    @Autowired
    protected SolrClient solrClient;

    @Autowired
    protected XmlMapper xmlMapper;

    @Autowired
    protected MockMvc mockMvc;

    private static final MongoContainer MONGO_CONTAINER;

    protected static final SolrContainer SOLR_CONTAINER;

    static {
        MONGO_CONTAINER = new MongoContainer("oaipmh-mongo")
                .withLogConsumer(new WaitingConsumer().andThen(new ToStringConsumer()));

        MONGO_CONTAINER.start();

        SOLR_CONTAINER = new SolrContainer("oaipmh-solr-search")
                .withLogConsumer(new WaitingConsumer().andThen(new ToStringConsumer()));

        SOLR_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongodb.connectionUrl", MONGO_CONTAINER::getConnectionUrl);
        registry.add("mongodb.record.dbname",  MONGO_CONTAINER::getRecordDb);
        registry.add("solr.url", SOLR_CONTAINER::getConnectionUrl);
        registry.add("zookeeper.url", () -> "");
        registry.add("solr.core", SOLR_CONTAINER::getSearchCore);

        registry.add("recordProviderClass", () -> "eu.europeana.oaipmh.service.DBRecordProvider");
        registry.add("enhanceWithTechnicalMetadata", () -> true);

        registry.add("resumptionTokenTTL", () -> RESUMPTION_TOKEN_TTL);
        registry.add("identifiersPerPage", () -> "10"); // total 26 records are saved, this is set to 10 to test the resumption token functionality
        registry.add("setsPerPage", () -> "2"); // we only have saved 5 sets in the solr container. hence the lower number

        registry.add("token_endpoint", () -> "");
        registry.add("grant_params", () -> "");

        // metadata format
        registry.add("metadata.formats.prefixes", () -> METADATA_FORMAT_PREFIX );
        registry.add("metadata.formats.converters.edm", () -> EU_EUROPEANA_OAIPMH_MODEL_METADATA_XML2_EDMCONVERTER);
        registry.add("metadata.formats.schemas.edm", () -> METADATA_FORMAT_SCHEMA);
        registry.add("metadata.formats.namespaces.edm", () -> METADATA_FORMAT_NAMESPACE);


        // identify
        registry.add("repositoryName", () -> REPOSITORY_NAME);
        registry.add("protocolVersion", () -> PROTOCOL_VERSION);
        registry.add("earliestDatestamp", () -> EARLIEST_DATESTAMP);
        registry.add("deletedRecord", () -> DELETED_RECORD);
        registry.add("granularity", () -> GRANULARITY);
        registry.add("adminEmail", () -> ADMIN_EMAIL);
        registry.add("compression", () -> COMPRESSION);
        registry.add("baseURL", () -> BASE_URL);
    }

    @BeforeAll
    public void init() throws Exception {
        // add solr documents
        addDocuments(SET_1, 5, DATE_1, DATE_3);
        addDocuments(SET_2, 7, DATE_2, DATE_3);
        addDocuments(SET_3, 8, DATE_3, null);
        addDocuments(SET_4, 3, DATE_1, DATE_2);
        addDocuments(SET_5, 3, DATE_3, null);

        checkIfDocumentsAreAdded();

        // add mongo data
    }

    private void checkIfDocumentsAreAdded() throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(50);
        QueryResponse response = solrClient.query(query);

        Assertions.assertNotNull(response, "documents fetched from solr are null");
        Assertions.assertEquals(26, response.getResults().size(), "Error while fetching the documents added during start up in solr");
    }

    public void addDocuments(String datasetId, int records, String creationDate, String modifiedDate) throws Exception {
        List<SolrInputDocument> docs = new ArrayList<>();
        List<FullBean> fullBeans = new ArrayList<>();

        for (int i = 1; i <= records; i++) {

            SolrInputDocument doc = new SolrInputDocument();

            doc.addField("europeana_id", "/" + datasetId + "/item_" + i);
            doc.addField("europeana_collectionName", datasetId + "_testCollection");
            doc.addField("edm_datasetName", datasetId + "_testCollection");
            doc.addField("timestamp_created", creationDate);
            doc.addField("timestamp_update", modifiedDate == null ? Instant.now().toString() : modifiedDate);

            docs.add(doc);
            fullBeans.add(getFullBean("/" + datasetId + "/item_" + i, creationDate, modifiedDate == null ? Instant.now().toString() : modifiedDate));
        }
        solrClient.add(docs);
        solrClient.commit();

        // add fullbeans
        ((DBRecordProvider) recordProvider).getRecordDao().getDatastore().save(fullBeans);
    }


    public <O> O readTestExample(Class<O> c, String testFilename) throws IOException {
        try (InputStream is = AbstractIntegrationIT.class.getClassLoader().getResourceAsStream(testFilename)) {
            return ( is == null ? null : xmlMapper.readValue(is, c) );
        }
    }
}
