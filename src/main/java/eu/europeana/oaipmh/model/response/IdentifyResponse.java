package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.model.ListIdentifiers;

import javax.xml.bind.annotation.XmlElement;

public class IdentifyResponse extends OAIResponse {

    public IdentifyResponse(String baseUrl, Identify identify) {
        super(baseUrl, identify);
    }

    @Override
    @XmlElement(name="Identify")
    public Identify getResponseObject() {
        return (Identify) super.getResponseObject();
    }
}
