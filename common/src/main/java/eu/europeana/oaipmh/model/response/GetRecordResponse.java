package eu.europeana.oaipmh.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

public class GetRecordResponse extends OAIResponse {

    private GetRecord getRecord;

    public GetRecordResponse() {}

    public GetRecordResponse(GetRecord getRecord, OAIRequest request) {
        super(request);
        this.getRecord = getRecord;
    }

    public void setGetRecord(GetRecord getRecord) {
        this.getRecord = getRecord;
    }

    @XmlElement(name="GetRecord")
    @JacksonXmlProperty(localName = "GetRecord")
    public GetRecord getGetRecord() {
        return getRecord;
    }
}
