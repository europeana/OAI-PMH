package eu.europeana.oaipmh.service;

import eu.europeana.metis.network.ExternalRequestUtil;
import eu.europeana.oaipmh.service.exception.BadArgumentException;
import eu.europeana.oaipmh.service.exception.ErrorCode;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.io.IOException;

import static eu.europeana.oaipmh.util.AppConfigConstants.OAI_PMH_SOLR_SERVICE;
import static eu.europeana.oaipmh.util.AppConfigConstants.SOLR_CLIENT_BEAN;

/**
 * Service class for interacting with a Solr client instance, facilitating queries and managing connections.
 * It implements the ClosableProvider interface for proper resource cleanup.
 */
@Service(OAI_PMH_SOLR_SERVICE)
public class SolrService implements ClosableProvider {
    private static final Logger LOG = LogManager.getLogger(SolrService.class);

    private final SolrClient client;

    @Autowired
    public SolrService(@Qualifier(SOLR_CLIENT_BEAN) SolrClient client) {
        this.client = client;
    }

    protected QueryResponse executeQuery(SolrQuery query) throws OaiPmhException {
        try {
            return ExternalRequestUtil.retryableExternalRequest(() -> {
                try {
                    return client.query(query);
                } catch(SolrException e){
                    LOG.debug("SolrException {}", e.getMessage());
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, truncateExceptionMessage(e.getMessage()));
                } catch (SolrServerException| IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SolrException e){
            throw new BadArgumentException(e.getMessage());
        } catch (RuntimeException e) {
            throw new OaiPmhException(e.getMessage(), ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public void close() {
        try {
            LOG.info("Destroying Solr client...");
            this.client.close();
        } catch (IOException e) {
            LOG.error("Solr client could not be closed.", e);
        }
    }

    // to truncate the error message thrown by Solr
    private String truncateExceptionMessage(String message){
        String errorMessage;
        if (StringUtils.contains(message , "org.apache.solr.search.SyntaxError")){
            errorMessage = StringUtils.substring(message, StringUtils.indexOf(message, "SyntaxError"), StringUtils.indexOf(message, "at line"));
        } else {
            errorMessage = "Exception sending request to search engine";
        }
        return errorMessage;
    }
}
