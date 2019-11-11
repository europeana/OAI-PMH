package eu.europeana.oaipmh.model.request;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class represents the ListIdentifiers verb request.
 */
public class ListIdentifiersRequest extends OAIRequest {

    private static final long serialVersionUID = 2761469193591873856L;

    @XmlAttribute
    private String metadataPrefix;

    @XmlAttribute
    private String set;

    @XmlAttribute
    private String from;

    @XmlAttribute
    private String until;

    @XmlAttribute
    private String resumptionToken;

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
