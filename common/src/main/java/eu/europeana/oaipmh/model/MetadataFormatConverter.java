package eu.europeana.oaipmh.model;

/**
 * General interface for metadata format converters
 */
public interface MetadataFormatConverter {

    /**
     * Convert record to the specific format
     * @param record record metadata
     * @return record metadata converted to the output format
     */
    String convert(String record);
}
