package eu.europeana.oaipmh.model.impl;

import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.SerializationConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Container for record xml information
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class GetRecordImpl implements GetRecord {

    @XmlElement(name=SerializationConstants.record)
    private Record record;

    protected GetRecordImpl() { this.record = null; }

    public GetRecordImpl(Record record) {
        this.record = record;
    }

    @Override
    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public static class Adapter extends XmlAdapter<GetRecordImpl, GetRecord> {

        public GetRecord unmarshal(GetRecordImpl v) { 
            return v; 
        }

        public GetRecordImpl marshal(GetRecord v) { 
            return (GetRecordImpl)v;
        }
    }
}
