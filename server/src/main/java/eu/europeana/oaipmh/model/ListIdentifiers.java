package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.ListIdentifiersResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="ListIdentifiers")
@XmlType(propOrder={"headers", "resumptionToken"})
public class ListIdentifiers extends OAIPMHVerb implements Serializable {

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
