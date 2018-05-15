package eu.europeana.oaipmh.model;

import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.GetRecordResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;

import javax.xml.bind.annotation.XmlElement;

/**
 * Container for record xml information
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
public class GetRecord extends OAIPMHVerb {

    private static final long serialVersionUID = -8111845326100870425L;

    @XmlElement
    private Record record;

    public GetRecord() { this.record = null; }

    public GetRecord(Record record) {
        this.record = record;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    @Override
    public OAIResponse getResponse(OAIRequest request) {
        return new GetRecordResponse(this, request);
    }
}
