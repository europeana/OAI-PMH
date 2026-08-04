package eu.europeana.oaipmh;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.oaipmh.model.metadata.MetadataFormatsService;
import eu.europeana.oaipmh.model.serialize.SerializationHandler;
import eu.europeana.oaipmh.model.serialize.ServerSerializationProvider;
import eu.europeana.oaipmh.service.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.SolrContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

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
public class AbstractIntegrationIT extends OaiPmhITConstants {

    private static final Logger logger = LogManager.getLogger(AbstractIntegrationIT.class);

    @Autowired
    protected RecordProvider recordProvider;

    @Autowired
    protected MetadataFormatsService metadataFormatsService;

    @Autowired
    protected IdentifyProvider identifyProvider;

    @InjectMocks
    protected DefaultSetsProvider setsProvider;

    @InjectMocks
    protected  SearchApi identifierProvider;

    protected static XmlMapper xmlMapper;

    @Autowired
    protected MockMvc mockMvc;

    // container with default dbname test
    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    //  used anywhere only added for the startu of application. we use mocked solr client later in all the IT as we can not save the data
//    @Container
//    static SolrContainer solr =
//            new SolrContainer(DockerImageName.parse("solr:9.9.0")) // we use solrj 8.11.3
//                    .withExposedPorts(8983)
//                    .withZookeeper(true)
//                    .withCommand("solr start -c") // The -c flag starts Solr in SolrCloud mode.
//                    .withCollection("testcollection")
//                    .withStartupTimeout(Duration.ofMinutes(3));
    // Create the solr container.
    @Container
    static SolrContainer solr = new SolrContainer(DockerImageName.parse("solr:8.11.3"));


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("mongodb.connectionUrl", mongo::getReplicaSetUrl);
        registry.add("mongodb.record.dbname", () -> "test");
        //"http://" + container.getHost() + ":" + container.getSolrPort() + "/solr"
        registry.add("solr.url", () -> "http://" + solr.getHost() + ":" + solr.getSolrPort() + "/solr");
        registry.add("zookeeper.url ", () -> "");
//        registry.add("solr.url", () -> "http://mock-solr:8983/solr");
        registry.add("solr.core", () -> "testcollection");

        registry.add("recordProviderClass", () -> "eu.europeana.oaipmh.service.DBRecordProvider");
        registry.add("enhanceWithTechnicalMetadata", () -> true);

        registry.add("resumptionTokenTTL", () -> RESUMPTION_TOKEN_TTL);
        registry.add("identifiersPerPage", () -> IDENTIFIERS_PER_PAGE);

        registry.add("token_endpoint", () -> "");
        registry.add("grant_params", () -> "");

        // metadata format
        registry.add("metadata.formats.prefixes", () -> METADATA_FORMAT_PREFIX);
        registry.add("metadata.formats.converters.format_prefix", () -> EU_EUROPEANA_OAIPMH_MODEL_METADATA_XML2_EDMCONVERTER);
        registry.add("metadata.formats.schemas.format_prefix", () -> METADATA_FORMAT_SCHEMA);
        registry.add("metadata.formats.namespaces.format_prefix", () -> METADATA_FORMAT_NAMESPACE);

        // identify
        registry.add("repositoryName", () -> REPOSITORY_NAME);
        registry.add("protocolVersion", () -> PROTOCOL_VERSION);
        registry.add("earliestDatestamp", () -> EARLIEST_DATESTAMP);
        registry.add("deletedRecord", () -> DELETED_RECORD);
        registry.add("deletedRecord", () -> DELETED_RECORD);
        registry.add("granularity", () -> GRANULARITY);
        registry.add("adminEmail", () -> ADMIN_EMAIL);
        registry.add("compression", () -> COMPRESSION);
        registry.add("baseUrl", () -> BASE_URL);
    }

    @BeforeAll
    public static void init() {
        mongo.start();
        solr.start();

        SerializationHandler.register(new ServerSerializationProvider());
        xmlMapper = SerializationHandler.getSerialization();
    }

    public static <O> O readTestExample(Class<O> c, String testFilename) throws IOException {
        try (InputStream is = AbstractIntegrationIT.class.getClassLoader().getResourceAsStream(testFilename)) {
            return ( is == null ? null : xmlMapper.readValue(is, c) );
        }
    }

    @AfterAll
    public static void tearDown() {
        logger.info(
                "Shutdown solr server : host = {}; port={}", solr.getHost(), solr.getMappedPort(8983) );
        solr.stop();
        logger.info("Shutdown mongo server : host = {}; port={}", mongo.getHost(), mongo.getReplicaSetUrl());
        mongo.stop();
    }
}
