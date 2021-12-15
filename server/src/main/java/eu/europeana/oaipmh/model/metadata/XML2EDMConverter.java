package eu.europeana.oaipmh.model.metadata;

import eu.europeana.oaipmh.model.MetadataFormatConverter;

/**
 * Basic XML to EDM metadata converter.
 */
public class XML2EDMConverter implements MetadataFormatConverter {

    @Override
    public String convert(String record) {
        return record;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return this.getClass() == obj.getClass();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
