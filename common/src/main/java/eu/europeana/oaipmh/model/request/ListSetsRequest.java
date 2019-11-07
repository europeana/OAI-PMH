package eu.europeana.oaipmh.model.request;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class represents the ListSets verb request.
 */
public class ListSetsRequest extends OAIRequest {

    private static final long serialVersionUID = -5685606349123941183L;

    @XmlAttribute
    private String resumptionToken;

    public ListSetsRequest(String verb, String baseUrl) {
        super(verb, baseUrl);
        this.resumptionToken = null;
    }

    public ListSetsRequest(String verb, String baseUrl, String resumptionToken) {
        this(verb, baseUrl);
        this.resumptionToken = resumptionToken;
    }

    public String getResumptionToken() {
        return resumptionToken;
    }

    public void setResumptionToken(String resumptionToken) {
        this.resumptionToken = resumptionToken;
    }
}
