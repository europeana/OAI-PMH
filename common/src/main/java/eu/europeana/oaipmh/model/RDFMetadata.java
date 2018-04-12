package eu.europeana.oaipmh.model;

public class RDFMetadata {
    public static final String METADATA = "metadata";

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
