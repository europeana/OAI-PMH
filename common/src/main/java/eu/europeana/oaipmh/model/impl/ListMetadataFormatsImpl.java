package eu.europeana.oaipmh.model.impl;

import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.MetadataFormat;
import eu.europeana.oaipmh.model.SerializationConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class represents the ListIdentifiers tag in the ListIdentifiers verb XML response
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListMetadataFormatsImpl implements ListMetadataFormats {

    @XmlElement(name=SerializationConstants.metadataFormat)
    private Collection<MetadataFormat> metadataFormats;

    public ListMetadataFormatsImpl() {
        this.metadataFormats = new ArrayList<>();
    }

    public ListMetadataFormatsImpl(Collection<MetadataFormat> metadataFormats) {
        this.metadataFormats = metadataFormats;
    }

    @Override
    public Collection<MetadataFormat> getMetadataFormats() {
        return metadataFormats;
    }

    public void setMetadataFormats(Collection<MetadataFormat> metadataFormats) {
        this.metadataFormats = metadataFormats;
    }

    @Override
    public void close() throws Exception {
    }

    public static class Adapter extends XmlAdapter<ListMetadataFormatsImpl
                                          , ListMetadataFormats> {
        public ListMetadataFormats unmarshal(ListMetadataFormatsImpl v) { 
            return v; 
        }

        public ListMetadataFormatsImpl marshal(ListMetadataFormats v) { 
            return (ListMetadataFormatsImpl)v;
        }
    }
}
