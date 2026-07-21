package eu.europeana.oaipmh.model;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.europeana.oaipmh.model.impl.GetRecordImpl;

/**
 * Container for record xml information
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
@XmlRootElement(name=GetRecord)
@XmlJavaTypeAdapter(GetRecordImpl.Adapter.class)
public interface GetRecord extends OAIPMHVerb {

    public Record getRecord();
}
