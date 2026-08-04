package eu.europeana.oaipmh.model.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import eu.europeana.oaipmh.model.response.OAIResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The SerializationTest class contains unit tests to verify the consistency of
 * XML serialization and deserialization using the SerializationHandler. It ensures
 * that the XML data remains consistent after being serialized and deserialized.
 *
 * This class also provides utility methods for reading test data, cleaning XML strings,
 * and handling the serialization/deserialization logic.
 */
public class SerializationTest {

    private static XmlMapper mapper;

    @BeforeAll
    static void setUp() {
        SerializationHandler.register(new DefaultSerializationProvider());
        mapper = SerializationHandler.getSerialization();
    }

    @Test
    public void testIdentify() throws IOException {
        test("/data/IdentifyResponse.xml");
    }

    @Test
    public void testListMetadataFormats() throws IOException {
        test("/data/ListMetadataFormatsResponse.xml");
    }

    @Test
    public void testListSets() throws IOException {
        test("/data/ListSetsResponse.xml");
    }

    @Test
    public void testListIdentifiers() throws IOException {
        test("/data/ListIdentifiersResponse.xml");
    }

    @Test
    public void testGetRecord() throws IOException {
        test("/data/GetRecordResponse.xml");
    }

    @Test
    public void testMetadataPrefixError() throws IOException {
        test("/data/MetadataPrefixErrorResponse.xml");
    }

    @Test
    public void testIdentifierError() throws IOException {
        test("/data/IdentifierErrorResponse.xml");
    }

    @Test
    public void testEmptyBadVerbError() throws IOException {
        test("/data/EmptyBadVerbErrorResponse.xml");
    }

    @Test
    public void testError() throws IOException {
        test("/data/ErrorResponse.xml");
    }

    /**
     * Tests the consistency of serialized and deserialized XML data for a given resource.
     * The method reads the test data from the specified resource, serializes and deserializes
     * the data, and asserts that the resulting data matches the original source data.
     *
     * @param resource the path to the resource file containing the test data
     * @throws IOException if an I/O error occurs while reading or processing the test data
     */
    private void test(String resource) throws IOException {
        String sourceData = readTestData(resource);
        String newData = serialiseOAIResponse(serialiseXmlString(sourceData));
        assertEquals(sourceData, newData, "source data " + resource + " not the same!");
    }

    /**
     * Reads and deserializes the given XML data string into an {@link OAIResponse} object.
     *
     * @param data the XML data string to be deserialized
     * @return an instance of {@link OAIResponse} deserialized from the provided XML data
     * @throws IOException if an I/O error occurs during deserialization
     */
    private OAIResponse serialiseXmlString(String data) throws IOException {
        return SerializationHandler.getSerialization()
                                   .readValue(data, OAIResponse.class);
    }

    /**
     * Reads test data from a specified resource file, processes it by cleaning
     * its XML data, and returns the resulting string.
     *
     * @param resource the path to the resource file containing the test data
     * @return the cleaned string representation of the test data
     * @throws RuntimeException if the resource cannot be found or if an I/O error occurs while reading the data
     */
    private String readTestData(String resource) {
        try (InputStream is = SerializationTest.class.getResourceAsStream(resource)) {
            if (is == null ) {
                throw new RuntimeException("Cannot find test data: " + resource);
            }
            return cleanXmlData(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Error reading test data: " + resource, e);
        }
    }

    /**
     * Cleans the provided XML data by removing unnecessary whitespace between XML tags.
     *
     * @param data the XML data as a string to be cleaned
     * @return the cleaned XML data with unnecessary whitespace between tags removed
     */
    private String cleanXmlData(String data) {
        return data.replaceAll("[>]\\s+[<]", "><");
    }

    /**
     * Serializes the given OAIResponse object to an XML string, cleans the serialized
     * XML by removing unnecessary whitespace, and returns the result.
     *
     * @param obj the OAIResponse object to be serialized and cleaned
     * @return the cleaned XML string representation of the serialized OAIResponse
     * @throws IOException if an I/O error occurs during serialization or writing
     */
    private String serialiseOAIResponse(OAIResponse obj) throws IOException {
        try (StringWriter writer = new StringWriter() ) {
            mapper.writeValue(writer, obj);
            return cleanXmlData(writer.toString());
        }
    }
}
