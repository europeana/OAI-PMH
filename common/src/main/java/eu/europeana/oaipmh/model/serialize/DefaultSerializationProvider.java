package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import eu.europeana.oaipmh.model.Metadata;
import eu.europeana.oaipmh.model.OAIPMHVerb;
import eu.europeana.oaipmh.model.impl.StreamListRecords;
import eu.europeana.oaipmh.model.response.OAIResponse;

public class DefaultSerializationProvider implements SerializationProvider {

    @Override
    public XmlMapper getSerialization() {
        XmlMapper xmlMapper = new XmlMapper(createJacksonModule());
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // not serialize fields with null value
        xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); // serialize also private fields
        // make sure dates are serialized in proper format
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        xmlMapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
        xmlMapper.setDateFormat(new ISO8601DateFormat()); // we set this to abbreviate the timezone (not sure how to use non-deprecated method for this)
        xmlMapper.registerModule(new JaxbAnnotationModule()); // so we can use JAX-B annotations instead of the Jackson ones
        return xmlMapper;
    }

    protected JacksonXmlModule createJacksonModule() {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);

        module.addDeserializer(OAIPMHVerb.class, new VerbDeserializer());
        module.addSerializer(OAIResponse.class, new OAIResponseSerializer());
        module.addDeserializer(Metadata.class, new StringMetadataDeserializer());
        module.addSerializer(Metadata.class, new MetadataSerializer());
        module.addSerializer(StreamListRecords.class, new StreamListRecordsSerializer());

        return module;
    }
}
