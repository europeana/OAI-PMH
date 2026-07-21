package eu.europeana.oaipmh.model.impl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.model.SerializationConstants;

/**
 * Container for identify data
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class IdentifyImpl implements Identify {

    // required fields
    @XmlElement(name=SerializationConstants.repositoryName)
    private String repositoryName;

    @XmlElement(name=SerializationConstants.baseURL)
    private String baseURL;

    @XmlElement(name=SerializationConstants.protocolVersion)
    private String protocolVersion;

    @XmlElement(name=SerializationConstants.earliestDatestamp)
    private String earliestDatestamp;

    @XmlElement(name=SerializationConstants.deletedRecord)
    private String deletedRecord;

    @XmlElement(name=SerializationConstants.granularity)
    private String granularity;

    @XmlElement(name=SerializationConstants.adminEmail)
    private String[] adminEmail;

    // optional fields
    @XmlElement(name="compression")
    private String[] compression;

    @XmlElement(name="description")
    private String[] description;

    // to support serialization
    protected IdentifyImpl() {}

    public IdentifyImpl(String repositoryName, String baseURL, 
            String protocolVersion, String earliestDatestamp, 
            String deletedRecord, String granularity, String[] adminEmail,
            String[] compression, String[] description) {
        this.repositoryName = repositoryName;
        this.baseURL = baseURL;
        this.protocolVersion = protocolVersion;
        this.earliestDatestamp = earliestDatestamp;
        this.deletedRecord = deletedRecord;
        this.granularity = granularity;
        this.adminEmail = adminEmail;
        this.compression = compression;
        this.description = description;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    @Override
    public String getRepositoryName() {
        return repositoryName;
    }

    public void setBaseURL(String baseUrl) {
        this.baseURL = baseUrl;
    }

    @Override
    public String getBaseURL() {
        return baseURL;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setEarliestDatestamp(String earliestDatestamp) {
        this.earliestDatestamp = earliestDatestamp;
    }

    @Override
    public String getEarliestDatestamp() {
        return earliestDatestamp;
    }

    public void setDeletedRecord(String deletedRecord) {
        this.deletedRecord = deletedRecord;
    }

    @Override
    public String getDeletedRecord() {
        return deletedRecord;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    @Override
    public String getGranularity() {
        return granularity;
    }

    public void setAdminEmail(String[] adminEmail) {
        this.adminEmail = adminEmail;
    }

    @Override
    public String[] getAdminEmail() {
        return adminEmail;
    }

    public void setCompression(String[] compression) {
        this.compression = compression;
    }

    @Override
    public String[] getCompression() {
        return compression;
    }

    public void setDescription(String[] description) {
        this.description = description;
    }

    @Override
    public String[] getDescription() {
        return description;
    }

    @Override
    public void close() throws Exception {
    }

    public static class Adapter extends XmlAdapter<IdentifyImpl, Identify> {

        public Identify unmarshal(IdentifyImpl v) { 
            return v; 
        }
        
        public IdentifyImpl marshal(Identify v) { 
            return (IdentifyImpl)v;
        }
    }
}
