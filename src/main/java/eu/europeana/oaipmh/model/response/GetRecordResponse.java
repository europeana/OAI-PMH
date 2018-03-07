package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.ListIdentifiers;

import javax.xml.bind.annotation.XmlElement;

public class GetRecordResponse extends OAIResponse {

    public GetRecordResponse(String baseUrl, GetRecord getRecord) {
        super(baseUrl, getRecord);
    }

    @Override
    @XmlElement(name="GetRecord")
    public GetRecord getResponseObject() {
        return (GetRecord) super.getResponseObject();
    }
}
