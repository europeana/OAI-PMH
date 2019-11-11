package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * This class represents metadataFormat tag in the ListMetadataFormats response XML
 */
public class MetadataFormat implements Serializable {

    private static final long serialVersionUID = 2145947131425894852L;

    @XmlElement
    private String metadataPrefix;

    @XmlElement
    private String schema;

    @XmlElement
    private String metadataNamespace;

    @JsonIgnore
    private transient MetadataFormatConverter converter;

    public MetadataFormat() {
        // empty constructor to allow deserialization
    }

    public MetadataFormat(String metadataPrefix, String schema, String metadataNamespace, MetadataFormatConverter converter) {
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
