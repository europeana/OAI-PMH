package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import eu.europeana.oaipmh.model.Metadata;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import java.io.IOException;

public class MetadataSerializer extends JsonSerializer<Metadata> {

    @Override
    public void serialize(Metadata meta, JsonGenerator gen
                        , SerializerProvider provider) throws IOException {
        gen.writeStartObject(metadata);
        writeMetadata(meta.getMetadata(), gen, provider);
        gen.writeEndObject();
    }

    private String removeXMLHeader(String xml) {
        int index = xml.indexOf("?>");
        return (index > -1 ? xml.substring(index + "?>".length()) : xml);
    }

    private void writeMetadata(Object obj, JsonGenerator gen
                             , SerializerProvider provider) 
            throws IOException {
        if ( obj instanceof String ) {
            gen.writeRaw(removeXMLHeader((String)obj));
            return;
        }

        JsonSerializer<Object> serializer = provider.findValueSerializer(obj.getClass());
        if ( serializer != null ) {
            serializer.serialize(obj, gen, provider);
        }
    }
}
