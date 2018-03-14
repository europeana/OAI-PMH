package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

public class ListIdentifiersResponse extends OAIResponse {

    public ListIdentifiersResponse(String baseUrl, ListIdentifiers listIdentifiers, OAIRequest request) {
        super(baseUrl, listIdentifiers, request);
    }

    @Override
    @XmlElement(name="ListIdentifiers")
    public ListIdentifiers getResponseObject() {
        return (ListIdentifiers) super.getResponseObject();
    }
}
