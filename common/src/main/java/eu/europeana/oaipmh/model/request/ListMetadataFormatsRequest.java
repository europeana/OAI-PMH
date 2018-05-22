package eu.europeana.oaipmh.model.request;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class represents the ListMetadataFormats verb request.
 */
public class ListMetadataFormatsRequest extends OAIRequest {
    @XmlAttribute
    private String identifier;

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
