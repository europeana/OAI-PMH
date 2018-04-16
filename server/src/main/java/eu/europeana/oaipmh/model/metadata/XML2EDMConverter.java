package eu.europeana.oaipmh.model.metadata;

/**
 * Basic XML to EDM metadata converter.
 */
public class XML2EDMConverter implements MetadataFormatConverter {

    @Override
    public String convert(String record) {
        return record;
    }
}
