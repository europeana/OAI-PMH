package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import eu.europeana.oaipmh.model.RDFMetadata;

import java.io.IOException;

import static eu.europeana.oaipmh.model.RDFMetadata.METADATA_TAG;

public class RDFMetadataSerializer extends JsonSerializer<RDFMetadata> {

    @Override
    public void serialize(RDFMetadata rdf, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject(METADATA_TAG);
        jsonGenerator.writeRaw(rdf.getMetadata());
        jsonGenerator.writeEndObject();
    }
}
