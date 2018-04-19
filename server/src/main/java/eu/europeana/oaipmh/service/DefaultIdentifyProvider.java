package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Identify;
import org.springframework.beans.factory.annotation.Value;

public class DefaultIdentifyProvider implements IdentifyProvider {

    @Value("${repositoryName}")
    private String repositoryName;

    @Value("${baseUrl}")
    private String baseUrl;

    @Value("${protocolVersion}")
    private String protocolVersion;

    @Value("${earliestDatestamp}")
    private String earliestDatestamp;

    @Value("${deletedRecord}")
    private String deletedRecord;

    @Value("${granularity}")
    private String granularity;

    @Value("${adminEmail}")
    private String[] adminEmail;

    // optional fields
    @Value("${compression}")
    private String[] compression;

    @Override
    public Identify provideIdentify() {
        Identify identify = new Identify();
        identify.setBaseUrl(baseUrl);
        identify.setAdminEmail(adminEmail);
        identify.setCompression(compression);
        identify.setDeletedRecord(deletedRecord);
        identify.setEarliestDatestamp(earliestDatestamp);
        identify.setGranularity(granularity);
        identify.setProtocolVersion(protocolVersion);
        identify.setRepositoryName(repositoryName);
        return identify;
    }
}
