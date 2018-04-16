package eu.europeana.oaipmh.model.metadata;

/**
 * All classes that provide metadata format must implement this interface. It is used to check whether provider is able
 * to disseminate object with the specified format and to retrieve the converter.
 */
public interface MetadataFormatProvider {
    boolean canDisseminate(String metadataFormat);

    MetadataFormatConverter getConverter(String metadataFormat);
}
