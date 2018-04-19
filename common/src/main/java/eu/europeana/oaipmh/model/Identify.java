package eu.europeana.oaipmh.model;

import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.IdentifyResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;

import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Container for identify data
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@XmlType(propOrder = {"repositoryName", "baseUrl", "protocolVersion", "adminEmail", "earliestDatestamp", "deletedRecord", "granularity", "compression", "description"})
public class Identify extends OAIPMHVerb implements Serializable {

    private static final long serialVersionUID = 203469625750930136L;

    // required fields
    private String repositoryName;

    private String baseUrl;

    private String protocolVersion;

    private String earliestDatestamp;

    private String deletedRecord;

    private String granularity;

    private String[] adminEmail;

    // optional fields
    private String[] compression;

    private String[] description;

    public Identify() {}

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setEarliestDatestamp(String earliestDatestamp) {
        this.earliestDatestamp = earliestDatestamp;
    }

    public String getEarliestDatestamp() {
        return earliestDatestamp;
    }

    public void setDeletedRecord(String deletedRecord) {
        this.deletedRecord = deletedRecord;
    }

    public String getDeletedRecord() {
        return deletedRecord;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setAdminEmail(String[] adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String[] getAdminEmail() {
        return adminEmail;
    }

    public void setCompression(String[] compression) {
        this.compression = compression;
    }

    public String[] getCompression() {
        return compression;
    }

    public void setDescription(String[] description) {
        this.description = description;
    }

    public String[] getDescription() {
        return description;
    }

    @Override
    public OAIResponse getResponse(OAIRequest request) {
        return new IdentifyResponse(this, request);
    }
}
