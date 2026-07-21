package eu.europeana.oaipmh.model.impl;

import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.SerializationConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class represents the ListRecords tag in the ListRecords verb XML response
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListRecordsImpl implements ListRecords {

    @XmlElement(name=SerializationConstants.record)
    private List<Record> records;

    @XmlElement(name=SerializationConstants.resumptionToken)
    private ResumptionToken resumptionToken;

    public ListRecordsImpl() {
        this.records = new ArrayList<>();
    }

    public ListRecordsImpl(List<Record> records, ResumptionToken resumptionToken) {
        this.records = records;
        this.resumptionToken = resumptionToken;
    }

    @Override
    public boolean isEmpty() {
        return records.isEmpty();
    }

    @Override
    public Stream<Record> stream() {
        return records.stream();
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    @Override
    public ResumptionToken getResumptionToken() { return resumptionToken; }

    public void setResumptionToken(ResumptionToken resumptionToken) { this.resumptionToken = resumptionToken; }

    @Override
    public void close() throws Exception {
    }

    public static class Adapter extends XmlAdapter<ListRecordsImpl
                                                 , ListRecords> {
        public ListRecords unmarshal(ListRecordsImpl v) { 
            return v; 
        }
        
        public ListRecordsImpl marshal(ListRecords v) { 
            return (ListRecordsImpl)v;
        }
    }
}
