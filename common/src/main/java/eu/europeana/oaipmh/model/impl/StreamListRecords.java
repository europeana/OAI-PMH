package eu.europeana.oaipmh.model.impl;

import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.SerializationConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.stream.Stream;

/**
 * This class represents the ListRecords tag in the ListRecords verb XML response
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class StreamListRecords implements ListRecords {

    @XmlElement(name=SerializationConstants.record)
    private Stream<Record> records;

    @XmlElement(name=SerializationConstants.resumptionToken)
    private ResumptionToken resumptionToken;

    protected StreamListRecords() {}

    public StreamListRecords(Stream<Record> records, ResumptionToken resumptionToken) {
        this.records = records;
        this.resumptionToken = resumptionToken;
    }

    @Override
    public boolean isEmpty() {
        return records != null;
    }

    @Override
    public Stream<Record> stream() { return records; }

    @Override
    public ResumptionToken getResumptionToken() { return resumptionToken; }

    @Override
    public void close() throws Exception {
        this.records.close();
    }
}
