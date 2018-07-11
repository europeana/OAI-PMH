package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.profile.TrackTime;
import eu.europeana.oaipmh.service.exception.InternalServerErrorException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;

public class SolrBasedProvider extends BaseProvider implements ClosableProvider {
    private static final Logger LOG = LogManager.getLogger(SolrBasedProvider.class);

    @Value("${resumptionTokenTTL}")
    private int resumptionTokenTTL;

    @Value("${solr.hosts}")
    private String solrHosts;

    @Value("${solr.zookeeperURL}")
    private String zookeeperURL;

    @Value("${solr.core}")
    private String solrCore;

    @Value("${solr.username}")
    private String username;

    @Value("${solr.password}")
    private String password;

    private CloudSolrClient client;

    /**
     * Initialize connection to Solr instance.
     */
    @PostConstruct
    private void init() throws InternalServerErrorException {
        LBHttpSolrClient lbTarget;
        try {
            lbTarget = new LBHttpSolrClient(solrHosts.split(","));
        } catch (MalformedURLException e) {
            LOG.error("Solr Server is not constructed!", e);
            throw new InternalServerErrorException(e.getMessage());
        }
        LOG.info("Using Zookeeper {} to connect to Solr cluster", zookeeperURL, solrHosts);
        client = new CloudSolrClient(zookeeperURL, lbTarget);
        client.setDefaultCollection(solrCore);
        client.connect();
        LOG.info("Connected to Solr {}", solrHosts);
    }

    @TrackTime
    protected QueryResponse executeQuery(SolrQuery query) throws OaiPmhException {
        try {
            return client.query(query);
        } catch (SolrServerException | IOException e) {
            throw new OaiPmhException(e.getMessage());
        }
    }

    protected int getResumptionTokenTTL() {
        return resumptionTokenTTL;
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
}
