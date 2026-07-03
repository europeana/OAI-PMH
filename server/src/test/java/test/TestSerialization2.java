package test;


import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.assertj.core.util.Arrays;
import org.checkerframework.checker.units.qual.t;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.Metadata;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.impl.ListMetadataFormatsImpl;
import eu.europeana.oaipmh.model.response.OAIResponse;
import eu.europeana.oaipmh.model.serialize.MetadataSerializer;
import eu.europeana.oaipmh.model.serialize.SerializationHandler;
import eu.europeana.oaipmh.model.serialize.ServerSerializationProvider;
import eu.europeana.oaipmh.model.serialize.DefaultSerializationProvider;
import eu.europeana.oaipmh.model.serialize.FullBeanSerializer;
import eu.europeana.oaipmh.model.serialize.StringMetadataDeserializer;
import eu.europeana.oaipmh.model.Record;


public class TestSerialization2 {

    static {
        SerializationHandler.register(new ServerSerializationProvider());
    }

    private static OAIResponse readTestExample(String testName) throws IOException {
        InputStream is = TestSerialization2.class.getResourceAsStream("/example/" + testName + ".xml");
        return ( is == null ? null : SerializationHandler.getSerialization().readValue(is, OAIResponse.class) );
    }

    private static void testClass(String testName) throws IOException {
        OAIResponse obj = readTestExample(testName);
        if ( obj == null ) { return; }
        SerializationHandler.getSerialization().writeValue(System.out, obj);
    }

    public static final void main(String[] args) throws IOException {
        testClass("data/Error");
        System.out.println();
        testClass("data/GetRecordResponse");
        System.out.println();
        //testClass(GetRecordResponse.class);
    }
}
