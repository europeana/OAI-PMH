package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.europeana.oaipmh.model.RDFMetadata;

import java.io.IOException;

import static eu.europeana.oaipmh.model.RDFMetadata.METADATA_TAG;

public class RDFMetadataDeserializer extends StdDeserializer<RDFMetadata> {

    private static final long serialVersionUID = 2626769524735903468L;

    public RDFMetadataDeserializer() {
        this(null);
    }

    private RDFMetadataDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public RDFMetadata deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode metadata = node.get(METADATA_TAG);
        if (metadata != null) {
            return new RDFMetadata(metadata.asText());
        }
        return new RDFMetadata(node.toString());
    }
}
