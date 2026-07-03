package eu.europeana.oaipmh.model.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import eu.europeana.oaipmh.model.SerializationConstants;

@XmlAccessorType(XmlAccessType.FIELD)
public class GetRecordRequest extends OAIRequest {

    @XmlAttribute(name=SerializationConstants.metadataPrefix)
    private String metadataPrefix;

    @XmlAttribute(name=SerializationConstants.identifier)
    private String identifier;

    protected GetRecordRequest() {}

    public GetRecordRequest(String verb, String baseUrl) {
        super(verb, baseUrl);
        this.metadataPrefix = null;
        this.identifier = null;
    }

    public GetRecordRequest(String verb, String baseUrl, String metadataPrefix, String identifier) {
        this(verb, baseUrl);
        this.metadataPrefix = metadataPrefix;
        this.identifier = identifier;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
