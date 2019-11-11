package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

public class IdentifyResponse extends OAIResponse {

    private static final long serialVersionUID = -2442426890956173353L;

    @XmlElement(name="Identify")
    private Identify responseObject;

    public IdentifyResponse() {}

    public IdentifyResponse(Identify identify, OAIRequest request) {
        super(request);
        this.responseObject = identify;
    }

    public void setResponseObject(Identify identify) {
        this.responseObject = identify;
    }

    public Identify getResponseObject() {
        return responseObject;
    }
}
