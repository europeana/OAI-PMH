package eu.europeana.oaipmh.model.request;

import javax.xml.bind.annotation.XmlAttribute;

public class GetRecordRequest extends OAIRequest {

    private static final long serialVersionUID = -2916674752350963926L;

    @XmlAttribute
    private String metadataPrefix;

    @XmlAttribute
    private String identifier;

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
