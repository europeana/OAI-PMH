package eu.europeana.oaipmh.model;

import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.ListRecordsResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the ListRecords tag in the ListRecords verb XML response
 */
@XmlRootElement(name="ListRecords")
@XmlType(propOrder={"records", "resumptionToken"})
public class ListRecords extends OAIPMHVerb {

    private static final long serialVersionUID = -8111845426100870425L;

    @XmlElement(name="record")
    private List<Record> records;

    @XmlElement
    private ResumptionToken resumptionToken;

    public ListRecords() {
        this.records = new ArrayList<>();
    }

    public ListRecords(List<Record> records, ResumptionToken resumptionToken) {
        this.records = records;
        this.resumptionToken = resumptionToken;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public ResumptionToken getResumptionToken() { return resumptionToken; }

    public void setResumptionToken(ResumptionToken resumptionToken) { this.resumptionToken = resumptionToken; }

    @Override
    public OAIResponse getResponse(OAIRequest request) {
        return new ListRecordsResponse(this, request);
    }
}
