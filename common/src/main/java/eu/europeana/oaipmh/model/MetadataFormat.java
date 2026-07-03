package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * This class represents metadataFormat tag in the ListMetadataFormats 
 * response XML
 */
@XmlRootElement(name=metadataFormat)
public class MetadataFormat {

    @XmlElement(name=SerializationConstants.metadataPrefix)
    private String metadataPrefix;

    @XmlElement(name=SerializationConstants.schema)
    private String schema;

    @XmlElement(name=SerializationConstants.metadataNamespace)
    private String metadataNamespace;

    @JsonIgnore
    private transient MetadataFormatConverter converter;

    // empty constructor to allow deserialization
    protected MetadataFormat() {}

    public MetadataFormat(String metadataPrefix, String schema
                        , String metadataNamespace
                        , MetadataFormatConverter converter) {
        this.metadataPrefix = metadataPrefix;
        this.schema = schema;
        this.metadataNamespace = metadataNamespace;
        this.converter = converter;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getMetadataNamespace() {
        return metadataNamespace;
    }

    public void setMetadataNamespace(String metadataNamespace) {
        this.metadataNamespace = metadataNamespace;
    }

    public MetadataFormatConverter getConverter() {
        return converter;
    }

    public void setConverter(MetadataFormatConverter converter) {
        this.converter = converter;
    }
}
