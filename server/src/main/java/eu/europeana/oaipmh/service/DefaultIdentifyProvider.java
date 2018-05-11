package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import eu.europeana.oaipmh.util.SolrQueryBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

import static eu.europeana.oaipmh.util.SolrConstants.TIMESTAMP_UPDATE;

public class DefaultIdentifyProvider extends SolrBasedProvider implements IdentifyProvider {

    @Value("${repositoryName}")
    private String repositoryName;

    @Value("${baseURL}")
    private String baseURL;

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
    public Identify provideIdentify() throws OaiPmhException {
        Identify identify = new Identify();
        identify.setBaseURL(baseURL);
        identify.setAdminEmail(adminEmail);
        identify.setCompression(compression);
        identify.setDeletedRecord(deletedRecord);
        identify.setEarliestDatestamp(getEarliestTimestamp());
        identify.setGranularity(granularity);
        identify.setProtocolVersion(protocolVersion);
        identify.setRepositoryName(repositoryName);
        return identify;
    }

    private String getEarliestTimestamp() throws OaiPmhException {
        QueryResponse response = executeQuery(SolrQueryBuilder.earliestTimestamp());
        SolrDocumentList results = response.getResults();
        for (SolrDocument doc : results) {
            Date value = (Date) doc.getFieldValue(TIMESTAMP_UPDATE);
            if (value != null) {
                return DateConverter.toIsoDate(value);
            }
        }
        return earliestDatestamp;
    }
}
