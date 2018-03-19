package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.OAIPMHVerb;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

public class ListIdentifiersResponse extends OAIResponse {

    public ListIdentifiersResponse() {}

    public ListIdentifiersResponse(ListIdentifiers listIdentifiers, OAIRequest request) {
        super(listIdentifiers, request);
    }

    public void setListIdentifiers(ListIdentifiers verb) {
        super.setResponseObject(verb);
    }

    public ListIdentifiers getListIdentifiers() { return getResponseObject(); }

    @Override
    @XmlElement(name="ListIdentifiers")
    public ListIdentifiers getResponseObject() {
        return (ListIdentifiers) super.getResponseObject();
    }
}
