package eu.europeana.oaipmh.model;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.europeana.oaipmh.model.impl.IdentifyImpl;

/**
 * Container for identify data
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@XmlRootElement(name=Identify)
@XmlJavaTypeAdapter(IdentifyImpl.Adapter.class)
@XmlType(propOrder = { repositoryName, baseURL, protocolVersion
                    , adminEmail, earliestDatestamp, deletedRecord
                    , granularity, compression, description })
public interface Identify extends OAIPMHVerb {

    public String getRepositoryName();

    public String getBaseURL();

    public String getProtocolVersion();

    public String getEarliestDatestamp();

    public String getDeletedRecord();

    public String getGranularity();

    public String[] getAdminEmail();

    public String[] getCompression();

    public String[] getDescription();
}
