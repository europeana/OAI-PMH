package eu.europeana.oaipmh.model.metadata;

public interface MetadataFormatProvider {
    boolean canDisseminate(String metadataFormat);

    MetadataFormatConverter getConverter(String metadataFormat);
}
