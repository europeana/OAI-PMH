package eu.europeana.oaipmh.model.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import eu.europeana.oaipmh.model.SerializationConstants;

/**
 * This class represents the ListMetadataFormats verb request.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListMetadataFormatsRequest extends OAIRequest {

    @XmlAttribute(name=SerializationConstants.identifier)
    private String identifier;

    protected ListMetadataFormatsRequest() {}

    public ListMetadataFormatsRequest(String verb, String baseUrl) {
        super(verb, baseUrl);
        this.identifier = null;
    }

    public ListMetadataFormatsRequest(String verb, String baseUrl, String identifier) {
        this(verb, baseUrl);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setMetadataPrefix(String identifier) {
        this.identifier = identifier;
    }
}
