package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.europeana.oaipmh.model.RDFMetadata;

import java.io.IOException;

import static eu.europeana.oaipmh.model.RDFMetadata.METADATA;

public class RDFMetadataDeserializer extends StdDeserializer<RDFMetadata> {

    public RDFMetadataDeserializer() {
        this(null);
    }

    public RDFMetadataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public RDFMetadata deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode metadata = node.get(METADATA);

        return new RDFMetadata(metadata.asText());
    }
}
