package eu.europeana.oaipmh.model.serialize;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import eu.europeana.oaipmh.model.response.OAIResponse;


@SpringBootTest
public class SerializationTest {

    static {
        SerializationHandler.register(new DefaultSerializationProvider());
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
    public void testError() throws IOException {
        test("/data/ErrorResponse.xml");
    }

    private void test(String rsrc) throws IOException {
        String srcData = null, newData = null;
        try {
            srcData = readTestData(rsrc);
            newData = runTest(readTestExample(srcData));
            assertEquals("source data " + rsrc + " not the same!"
                       , srcData, newData);
        }
        catch (AssertionError e) {
            System.out.println("--- SOURCE DATA ---");
            System.out.println(srcData);
            System.out.println();
            System.out.println("---- NEW DATA -----");
            System.out.println(newData);
            throw e;
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private OAIResponse readTestExample(String data) throws IOException {
        return SerializationHandler.getSerialization()
                                   .readValue(data, OAIResponse.class);
    }

    private String readTestData(String rsrc) {
        try {
            InputStream is = SerializationTest.class.getResourceAsStream(rsrc);
            if (is == null ) { 
                throw new RuntimeException("Cannot find test data: " + rsrc);
            }
            return cleanXmlData(new String(is.readAllBytes()
                                         , StandardCharsets.UTF_8)); 
        } catch (IOException e) {
            throw new RuntimeException("Error reading test data: " + rsrc, e);
        }
    }

    private String cleanXmlData(String data) {
        return data.replaceAll("[>]\\s+[<]", "><");
    }

    private String runTest(OAIResponse obj) throws IOException {
        try (StringWriter writer = new StringWriter() ) {
            SerializationHandler.getSerialization().writeValue(writer, obj);
            return cleanXmlData(writer.toString());
        }
    }
}
