package eu.europeana.oaipmh.service;

import eu.europeana.metis.network.ExternalRequestUtil;
import eu.europeana.oaipmh.service.exception.BadArgumentException;
import eu.europeana.oaipmh.service.exception.ErrorCode;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A provider implementation that integrates with an Apache Solr cluster to
 * execute queries and manage connections for OAI-PMH operations. This provider
 * extends {@code BaseProvider} and implements {@code ClosableProvider} to
 * provide lifecycle management for the Solr client.
 */
public class SolrBasedProvider extends BaseProvider implements ClosableProvider {
    private static final Logger LOG = LogManager.getLogger(SolrBasedProvider.class);

    private CloudSolrClient client;

    /**
     * Initialize connection to Solr instance.
     */
    @PostConstruct
    private void init() {
        LOG.info("Connecting to Solr cluster: {}...", settings.getSolrUrl());

        List<String> solrHosts;
        if (settings.getSolrUrl().contains(",")) {
            solrHosts = Arrays.asList(settings.getSolrUrl().split(","));
        } else {
            solrHosts = new ArrayList<>();
            solrHosts.add(settings.getSolrUrl());
        }

        client = new CloudSolrClient.Builder(solrHosts).build();
        client.setDefaultCollection(settings.getSolrCore());
        client.connect();
        LOG.info("Connected to Solr {}", settings.getSolrUrl());
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
