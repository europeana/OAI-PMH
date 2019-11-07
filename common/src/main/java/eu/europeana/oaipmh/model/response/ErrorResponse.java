package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.OAIError;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

public class ErrorResponse extends OAIResponse {

    private static final long serialVersionUID = 4176761060044789096L;

    @XmlElement
    private OAIError error;

    public ErrorResponse() {}

    public ErrorResponse(OAIError error, OAIRequest request) {
        super(request);
        this.error = error;
    }

    public OAIError getError() {
        return error;
    }

    public void setError(OAIError error) {
        this.error = error;
    }
}
