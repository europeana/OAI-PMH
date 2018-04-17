package eu.europeana.oaipmh.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import eu.europeana.oaipmh.model.response.OAIResponse;
import eu.europeana.oaipmh.service.exception.SerializationException;

import java.io.IOException;

public class BaseService {

    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    // create a single XmlMapper for efficiency purposes (see https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance)
    private static final XmlMapper xmlMapper;

    static {
        JacksonXmlModule module = new JacksonXmlModule();
        // using "unwrapped" Lists:
        module.setDefaultUseWrapper(false);
        xmlMapper = new XmlMapper(module);
    }

    public BaseService() {
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // not serialize fields with null value
        xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); // serialize also private fields
        // make sure dates are serialized in proper format
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        xmlMapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
        xmlMapper.setDateFormat(new ISO8601DateFormat()); // we set this to abbreviate the timezone (not sure how to use non-deprecated method for this)
        xmlMapper.registerModule(new JaxbAnnotationModule()); // so we can use JAX-B annotations instead of the Jackson ones
    }

    protected XmlMapper getXmlMapper() {
        return xmlMapper;
    }

    protected String serialize(OAIResponse response) throws SerializationException {
        try {
            return XML_DECLARATION + xmlMapper.
                    writerWithDefaultPrettyPrinter().
                    writeValueAsString(response);
        }
        catch (IOException e) {
            throw new SerializationException("Error serializing data: " + e.getMessage(), e);
        }
    }
}
