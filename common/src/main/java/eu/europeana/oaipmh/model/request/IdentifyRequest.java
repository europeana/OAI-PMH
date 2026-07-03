package eu.europeana.oaipmh.model.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class IdentifyRequest extends OAIRequest {

    protected IdentifyRequest() {}

    public IdentifyRequest(String verb, String baseUrl) {
        super(verb, baseUrl);
    }
}
