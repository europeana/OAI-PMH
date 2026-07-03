package eu.europeana.oaipmh.model.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import eu.europeana.oaipmh.model.SerializationConstants;

/**
 * This class represents the ListSets verb request.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListSetsRequest extends OAIRequest {

    @XmlAttribute(name=SerializationConstants.from)
    private String from;

    @XmlAttribute(name=SerializationConstants.until)
    private String until;

    @XmlAttribute(name=SerializationConstants.resumptionToken)
    private String resumptionToken;

    protected ListSetsRequest() {}

    public ListSetsRequest(String verb, String baseUrl) {
        super(verb, baseUrl);
        this.from = null;
        this.until = null;
        this.resumptionToken = null;
    }

    public ListSetsRequest(String verb, String baseUrl, String from, String until) {
        super(verb, baseUrl);
        this.from = from;
        this.until = until;
    }

    public ListSetsRequest(String verb, String baseUrl, String resumptionToken) {
        this(verb, baseUrl);
        this.resumptionToken = resumptionToken;
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
