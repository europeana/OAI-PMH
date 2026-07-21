package eu.europeana.oaipmh.model.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import eu.europeana.oaipmh.model.SerializationConstants;

/**
 * This class represents the ListIdentifiers verb request.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListIdentifiersRequest extends OAIRequest {

    @XmlAttribute(name=SerializationConstants.metadataPrefix)
    private String metadataPrefix;

    @XmlAttribute(name=SerializationConstants.set)
    private String set;

    @XmlAttribute(name=SerializationConstants.from)
    private String from;

    @XmlAttribute(name=SerializationConstants.until)
    private String until;

    @XmlAttribute(name=SerializationConstants.resumptionToken)
    private String resumptionToken;

    protected ListIdentifiersRequest() {}

    public ListIdentifiersRequest(String verb, String baseUrl) {
        super(verb, baseUrl);
        this.metadataPrefix = null;
        this.set = null;
        this.from = null;
        this.until = null;
        this.resumptionToken = null;
    }

    public ListIdentifiersRequest(String verb, String baseUrl, String metadataPrefix, String set, String from, String until) {
        this(verb, baseUrl);
        this.metadataPrefix = metadataPrefix;
        this.set = set;
        this.from = from;
        this.until = until;
    }

    public ListIdentifiersRequest(String verb, String baseUrl, String resumptionToken) {
        this(verb, baseUrl);
        this.resumptionToken = resumptionToken;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getUntil() {
        return until;
    }

    public void setUntil(String until) {
        this.until = until;
    }

    public String getResumptionToken() {
        return resumptionToken;
    }

    public void setResumptionToken(String resumptionToken) {
        this.resumptionToken = resumptionToken;
    }
}
