package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.europeana.oaipmh.model.serialize.RDFMetadataDeserializer;
import java.io.Serializable;

@JsonDeserialize(using = RDFMetadataDeserializer.class)
public class RDFMetadata implements Serializable {

    public static final String METADATA_TAG = "metadata";

    private static final long serialVersionUID = -4458812803376020340L;

    private String metadata;

    public RDFMetadata() {}

    public RDFMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
