package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.model.impl.IdentifyImpl;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
import eu.europeana.oaipmh.util.SolrQueryBuilder;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import javax.annotation.Resource;
import java.util.Date;

import static eu.europeana.oaipmh.util.AppConfigConstants.OAI_PMH_SOLR_SERVICE;
import static eu.europeana.oaipmh.util.SolrConstants.TIMESTAMP_UPDATE;

public class DefaultIdentifyProvider 
        extends BaseProvider implements IdentifyProvider {

    @Resource(name = OAI_PMH_SOLR_SERVICE)
    SolrService solrService;


    @Override
    public Identify provideIdentify() throws OaiPmhException {
        return new IdentifyImpl(
            settings.getRepositoryName(), settings.getBaseUrl(),
            settings.getProtocolVersion(), getEarliestTimestamp(),
            settings.getDeletedRecord(), settings.getGranularity(), settings.getAdminEmail(),
            settings.getCompression(), null);
    }

    private String getEarliestTimestamp() throws OaiPmhException {
        QueryResponse response = solrService.executeQuery(SolrQueryBuilder.earliestTimestamp());
        SolrDocumentList results = response.getResults();
        for (SolrDocument doc : results) {
            Date value = (Date) doc.getFieldValue(TIMESTAMP_UPDATE);
            if (value != null) {
                return DateConverter.toIsoDate(value);
            }
        }
        return settings.getEarliestDatestamp();
    }
}
