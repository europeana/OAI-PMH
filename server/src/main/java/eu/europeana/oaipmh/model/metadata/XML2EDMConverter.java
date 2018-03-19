package eu.europeana.oaipmh.model.metadata;

public class XML2EDMConverter implements MetadataFormatConverter {

    @Override
    public String convert(String record) {
        return record;
    }
}
