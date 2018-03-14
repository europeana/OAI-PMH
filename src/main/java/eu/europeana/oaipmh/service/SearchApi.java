package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.service.exception.ErrorCode;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.ResumptionTokenHelper;
import eu.europeana.oaipmh.util.SolrQueryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.europeana.oaipmh.util.SolrConstants.*;

/**
 * Retrieve information from Search API
 */
public class SearchApi implements IdentifierProvider {

    private static final Logger LOG = LogManager.getLogger(SearchApi.class);

    @Value("${identifiersPerPage}")
    private int identifiersPerPage;

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

    @PostConstruct
    private void init() {
        LBHttpSolrClient lbTarget;
        try {
            lbTarget = new LBHttpSolrClient(solrHosts.split(","));
        } catch (MalformedURLException e) {
            LOG.error("Solr Server is not constructed!", e);
            throw new RuntimeException(e);
        }
        LOG.info("Using Zookeeper {} to connect to Solr cluster", zookeeperURL, solrHosts);
        client = new CloudSolrClient(zookeeperURL, lbTarget);
        client.setDefaultCollection(solrCore);
        client.connect();
        LOG.info("Connected to Solr {}", solrHosts);
    }

    @Override
    public ListIdentifiers listIdentifiers(String metadataPrefix, Date from, Date until, String set) throws OaiPmhException {
        LOG.info("List identifiers: from {}, until {}, set {}, metadataPrefix {}", from, until, set, metadataPrefix);

        SolrQuery query = SolrQueryBuilder.listIdentifiers(from, until, set, identifiersPerPage);
        return listIdentifiers(query, 0, null);
    }

    @Override
    public ListIdentifiers listIdentifiers(String resumptionToken) throws OaiPmhException {
        ResumptionToken temporaryToken = ResumptionTokenHelper.decodeResumptionToken(resumptionToken);
        validateResumptionToken(temporaryToken.getExpirationDate());

        LOG.info("List identifiers: resumption token {}", resumptionToken);

        SolrQuery query = SolrQueryBuilder.listIdentifiers(temporaryToken.getFilterQuery(), temporaryToken.getValue(), identifiersPerPage);
        return listIdentifiers(query, temporaryToken.getCursor() + identifiersPerPage, temporaryToken.getValue());
    }

    private ListIdentifiers listIdentifiers(SolrQuery query, long cursor, String previousCursorMark) throws OaiPmhException {
        try {
            QueryResponse response = client.query(query);
            return responseToListIdentifiers(response, cursor, previousCursorMark);
        } catch (SolrServerException | IOException e) {
            throw new OaiPmhException(e.getMessage());
        }
    }

    private void validateResumptionToken(Date expirationDate) throws OaiPmhException {
        if (new Date().after(expirationDate)) {
            throw new OaiPmhException("Resumption token expired ad " + expirationDate, ErrorCode.BAD_RESUMPTION_TOKEN);
        }
    }

    private ListIdentifiers responseToListIdentifiers(QueryResponse response, long cursor, String previousCursorMark) {
        List<Header> headers = new ArrayList<>();

        SolrDocumentList docs = response.getResults();
        for (SolrDocument document : docs) {
            headers.add(documentToHeader(document));
        }

        ResumptionToken resumptionToken;
        if (shouldCreateResumptionToken(response, cursor, previousCursorMark)) {
            resumptionToken = ResumptionTokenHelper.createResumptionToken(response.getNextCursorMark(),
                    docs.getNumFound(), new Date(System.currentTimeMillis() + resumptionTokenTTL), cursor, getFilterQuery(response));
        } else {
            resumptionToken = null;
        }
        return new ListIdentifiers(headers, resumptionToken);
    }

    private boolean shouldCreateResumptionToken(QueryResponse response, long cursor, String previousCursorMark) {
        // just one page of results
        if (previousCursorMark == null && response.getResults().size() == response.getResults().getNumFound()) {
            return false;
        }
        // last page
        return !response.getNextCursorMark().equals(previousCursorMark) && response.getResults().size() + cursor != response.getResults().getNumFound();
    }

    private List<String> getFilterQuery(QueryResponse response) {
        Object fq = ((NamedList) response.getHeader().get(PARAMS)).get(CommonParams.FQ);
        List<String> list = new ArrayList<>();
        if (fq == null) {
            return list;
        }
        if (fq instanceof String) {
            list.add((String) fq);
            return list;
        }
        return (List<String>) fq;
    }

    private Header documentToHeader(SolrDocument document) {
        List<String> sets = new ArrayList<>();
        for (Object value : document.getFieldValues(DATASET_NAME)) {
            sets.add((String) value);
        }
        return new Header((String) document.getFieldValue(EUROPEANA_ID), (Date) document.getFieldValue(TIMESTAMP), sets);
    }

    @Override
    public void close() {

    }
}
