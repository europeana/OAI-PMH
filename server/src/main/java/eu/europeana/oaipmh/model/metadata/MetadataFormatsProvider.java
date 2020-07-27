package eu.europeana.oaipmh.model.metadata;

import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.MetadataFormatConverter;

/**
 * All classes that provide metadata format must implement this interface. It is used to check whether provider is able
 * to disseminate object with the specified format and to retrieve the converter.
 */
public interface MetadataFormatsProvider {
    boolean canDisseminate(String metadataFormat);

    MetadataFormatConverter getConverter(String metadataFormat);

    ListMetadataFormats listMetadataFormats() ;
}
