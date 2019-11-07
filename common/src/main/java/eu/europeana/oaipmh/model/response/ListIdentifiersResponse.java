package eu.europeana.oaipmh.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

/**
 * The class represents the response for the ListIdentifiers verb request
 */
public class ListIdentifiersResponse extends OAIResponse {

    private static final long serialVersionUID = -4650031724659908913L;

    private ListIdentifiers listIdentifiers;

    public ListIdentifiersResponse() {}

    public ListIdentifiersResponse(ListIdentifiers listIdentifiers, OAIRequest request) {
        super(request);
        this.listIdentifiers = listIdentifiers;
    }

    public void setListIdentifiers(ListIdentifiers listIdentifiers) {
        this.listIdentifiers = listIdentifiers;
    }

    @XmlElement(name="ListIdentifiers")
    @JacksonXmlProperty(localName = "ListIdentifiers")
    public ListIdentifiers getListIdentifiers() {
        return listIdentifiers;
    }
}
