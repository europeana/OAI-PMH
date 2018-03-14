package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

public class IdentifyResponse extends OAIResponse {

    public IdentifyResponse(String baseUrl, Identify identify, OAIRequest request) {
        super(baseUrl, identify, request);
    }

    @Override
    @XmlElement(name="Identify")
    public Identify getResponseObject() {
        return (Identify) super.getResponseObject();
    }
}
