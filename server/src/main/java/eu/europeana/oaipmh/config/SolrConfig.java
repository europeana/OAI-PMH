package eu.europeana.oaipmh.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static eu.europeana.oaipmh.util.AppConfigConstants.SOLR_CLIENT_BEAN;

@Configuration
public class SolrConfig {

    private static final Logger LOG = LogManager.getLogger(SolrConfig.class);

    @Resource
    OaiPmhSettings settings;

    @Bean(SOLR_CLIENT_BEAN)
    public SolrClient solrClient() {
        if (StringUtils.isNotBlank(settings.getZookeeperURL())) {
            return initSolrCloudClient(settings.getZookeeperURL(), settings.getSolrTimeout(),
                    settings.getSolrCore());
        } else {
            return initSolrClient(settings.getSolrUrl(), settings.getSolrTimeout());
        }
    }

    private SolrClient initSolrClient(String solrUrl, int timeoutMillis) {
        LOG.info(
                "Configuring solr client at the url: {}", solrUrl);

        if (solrUrl.contains(",")) {
            LBHttpSolrClient.Builder builder = new LBHttpSolrClient.Builder();
            return builder
                    .withBaseSolrUrls(solrUrl.split(","))
                    .withConnectionTimeout(timeoutMillis)
                    .build();
        } else {
            HttpSolrClient.Builder builder = new HttpSolrClient.Builder();
            return builder
                    .withBaseSolrUrl(solrUrl)
                    .withConnectionTimeout(timeoutMillis)
                    .build();
        }
    }

    private SolrClient initSolrCloudClient(String solrZookeeperUrl, int timeout, String solrCollection) {
        LOG.info(
                "Configuring solr client with the zookeperurls: {} and collection: {}",
                solrZookeeperUrl,
                solrCollection);

        String[] solrZookeeperUrlsList = solrZookeeperUrl.trim().split(",");

        CloudSolrClient client =
                new CloudSolrClient.Builder(Arrays.asList(solrZookeeperUrlsList), Optional.empty())
                        .withConnectionTimeout(timeout)
                        .build();

        client.setDefaultCollection(solrCollection);
        return client;
    }

}
