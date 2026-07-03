package eu.europeana.oaipmh.model;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import java.util.stream.Stream;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.europeana.oaipmh.model.impl.ListRecordsImpl;

/**
 * This class represents the ListRecords tag in the ListRecords verb XML response
 */
@XmlRootElement(name=ListRecords)
@XmlJavaTypeAdapter(ListRecordsImpl.Adapter.class)
@XmlType(propOrder={ records, resumptionToken })
public interface ListRecords extends OAIPMHVerb, AutoCloseable {

    public boolean isEmpty();

    public Stream<Record> stream();

    public ResumptionToken getResumptionToken();
}
