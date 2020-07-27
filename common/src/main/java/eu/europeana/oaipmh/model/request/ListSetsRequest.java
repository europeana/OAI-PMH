package eu.europeana.oaipmh.model.request;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class represents the ListSets verb request.
 */
public class ListSetsRequest extends OAIRequest {

    private static final long serialVersionUID = -5685606349123941183L;

    @XmlAttribute
    private String from;

    @XmlAttribute
    private String until;

    @XmlAttribute
    private String resumptionToken;

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
