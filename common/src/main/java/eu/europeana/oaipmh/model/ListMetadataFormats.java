package eu.europeana.oaipmh.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.europeana.oaipmh.model.impl.ListMetadataFormatsImpl;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import java.util.Collection;

/**
 * This class represents the ListMetadataFormats tag in 
 * the ListMetadataFormats verb XML response
 */
@XmlRootElement(name=ListMetadataFormats)
@XmlJavaTypeAdapter(ListMetadataFormatsImpl.Adapter.class)
public interface ListMetadataFormats extends OAIPMHVerb {

    public Collection<MetadataFormat> getMetadataFormats();
}
