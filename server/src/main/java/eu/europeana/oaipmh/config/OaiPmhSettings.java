package eu.europeana.oaipmh.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Date;

@Configuration
@PropertySource("classpath:oai-pmh.properties")
@PropertySource(value = "classpath:oai-pmh.user.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:build.properties", ignoreResourceNotFound = true)
public class OaiPmhSettings {

    @Value("${baseURL}")
    private String baseUrl;

    @Value("${identifierPrefix}")
    private String identifierPrefix;

    @Value("${mongodb.connectionUrl}")
    private String connectionUrl;

    @Value("${mongodb.record.dbname}")
    private String recordDBName;

    @Value("${repositoryName}")
    private String repositoryName;

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

    @Value("${setsPerPage}")
    private int setsPerPage;

    @Value("${recordsPerPage}")
    private int recordsPerPage;

    @Value("${identifiersPerPage}")
    private int identifiersPerPage;

    @Value("${resumptionTokenTTL}")
    private int resumptionTokenTTL;


    @Value("${recordApiUrl}")
    private String recordApiUrl;

    @Value("${wskey}")
    private String wskey;

    @Value("${solr.url}")
    private String solrUrl;

    @Value("${zookeeper.url}")
    private String zookeeperURL;

    @Value("${solr.core}")
    private String solrCore;

    @Value("#{T(eu.europeana.oaipmh.util.DateConverter).fromIsoDateTime('${defaultIdentifierTimestamp}')}")
    private Date defaultIdentifierTimestamp;


    @Value("${recordProviderClass}")
    private String recordProviderClass;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getIdentifierPrefix() {
        return identifierPrefix;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public String getRecordDBName() {
        return recordDBName;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getEarliestDatestamp() {
        return earliestDatestamp;
    }

    public String getDeletedRecord() {
        return deletedRecord;
    }

    public String getGranularity() {
        return granularity;
    }

    public String[] getAdminEmail() {
        return adminEmail;
    }

    public String[] getCompression() {
        return compression;
    }

    public int getSetsPerPage() {
        return setsPerPage;
    }

    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public int getIdentifiersPerPage() {
        return identifiersPerPage;
    }

    public int getResumptionTokenTTL() {
        return resumptionTokenTTL;
    }

    public String getRecordApiUrl() {
        return recordApiUrl;
    }

    public String getWskey() {
        return wskey;
    }

    public String getSolrUrl() {
        return solrUrl;
    }

    public String getZookeeperURL() {
        return zookeeperURL;
    }

    public String getSolrCore() {
        return solrCore;
    }

    public Date getDefaultIdentifierTimestamp() {
        return defaultIdentifierTimestamp;
    }

    public String getRecordProviderClass() {
        return recordProviderClass;
    }
}
