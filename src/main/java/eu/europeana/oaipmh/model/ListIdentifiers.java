package eu.europeana.oaipmh.model;

import eu.europeana.oaipmh.model.response.ListIdentifiersResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="ListIdentifiers")
public class ListIdentifiers extends OAIPMHVerb implements Serializable {

    private static final long serialVersionUID = -8111855326100870425L;

    private List<Header> headers;

    public ListIdentifiers() {
        this.headers = new ArrayList<>();
    }

    public ListIdentifiers(List<Header> headers) {
        this.headers = headers;
    }

    @XmlElement(name="header")
    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    @Override
    public OAIResponse getResponse(String baseUrl) {
        return new ListIdentifiersResponse(baseUrl, this);
    }
}
