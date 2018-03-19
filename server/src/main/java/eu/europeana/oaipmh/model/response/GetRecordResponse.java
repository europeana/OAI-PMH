package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

public class GetRecordResponse extends OAIResponse {

    public GetRecordResponse(GetRecord getRecord, OAIRequest request) {
        super(getRecord, request);
    }

    @Override
    @XmlElement(name="GetRecord")
    public GetRecord getResponseObject() {
        return (GetRecord) super.getResponseObject();
    }
}
