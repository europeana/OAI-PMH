package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.ListIdentifiers;

import javax.xml.bind.annotation.XmlElement;

public class ListIdentifiersResponse extends OAIResponse {

    public ListIdentifiersResponse(String baseUrl, ListIdentifiers listIdentifiers) {
        super(baseUrl, listIdentifiers);
    }

    @Override
    @XmlElement(name="ListIdentifiers")
    public ListIdentifiers getResponseObject() {
        return (ListIdentifiers) super.getResponseObject();
    }
}
