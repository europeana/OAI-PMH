package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

public class GetRecordResponse extends OAIResponse {

    @XmlElement(name="GetRecord")
    private GetRecord responseObject;

    public GetRecordResponse() {}

    public GetRecordResponse(GetRecord getRecord, OAIRequest request) {
        super(request);
        this.responseObject = getRecord;
    }

    public void setResponseObject(GetRecord getRecord) {
        this.responseObject = getRecord;
    }

    public GetRecord getResponseObject() {
        return responseObject;
    }
}
