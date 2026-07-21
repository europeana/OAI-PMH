package eu.europeana.oaipmh.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.europeana.oaipmh.model.impl.ListSetsImpl;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import java.util.stream.Stream;

/**
 * This class represents the ListSets tag in the ListSets verb XML response
 */
@XmlRootElement(name=ListSets)
@XmlJavaTypeAdapter(ListSetsImpl.Adapter.class)
@XmlType(propOrder={ sets , resumptionToken })
public interface ListSets extends OAIPMHVerb, AutoCloseable {

    public boolean isEmpty();

    public Stream<Set> stream();

    public ResumptionToken getResumptionToken();
}
