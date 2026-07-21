package eu.europeana.oaipmh.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.europeana.oaipmh.model.impl.ListIdentifiersImpl;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import java.util.stream.Stream;

/**
 * This class represents the ListIdentifiers tag in the ListIdentifiers verb XML response
 */
@XmlRootElement(name=ListIdentifiers)
@XmlJavaTypeAdapter(ListIdentifiersImpl.Adapter.class)
@XmlType(propOrder={ headers, resumptionToken })
public interface ListIdentifiers extends OAIPMHVerb {

    public boolean isEmpty();

    public Stream<Header> stream();

    public ResumptionToken getResumptionToken();
}
