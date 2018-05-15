package eu.europeana.oaipmh.model;

import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.ListIdentifiersResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the ListIdentifiers tag in the ListIdentifiers verb XML response
 */
@XmlRootElement(name="ListIdentifiers")
@XmlType(propOrder={"headers", "resumptionToken"})
public class ListIdentifiers extends OAIPMHVerb {

    private static final long serialVersionUID = -8111855326100870425L;

    @XmlElement(name="header")
    private List<Header> headers;

    @XmlElement
    private ResumptionToken resumptionToken;

    public ListIdentifiers() {
        this.headers = new ArrayList<>();
    }

    public ListIdentifiers(List<Header> headers, ResumptionToken resumptionToken) {
        this.headers = headers;
        this.resumptionToken = resumptionToken;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public ResumptionToken getResumptionToken() { return resumptionToken; }

    public void setResumptionToken(ResumptionToken resumptionToken) { this.resumptionToken = resumptionToken; }

    @Override
    public OAIResponse getResponse(OAIRequest request) {
        return new ListIdentifiersResponse(this, request);
    }
}
