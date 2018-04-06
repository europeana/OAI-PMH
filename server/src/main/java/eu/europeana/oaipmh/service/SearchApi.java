package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.service.exception.NoRecordsMatchException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.DateConverter;
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

    private static final Date DEFAULT_IDENTIFIER_TIMESTAMP = DateConverter.fromIsoDateTime("1970-01-01T00:00:00Z");

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

    @Value("#{T(eu.europeana.oaipmh.util.DateConverter).fromIsoDateTime('${defaultIdentifierTimestamp}')}")
    private Date defaultIdentifierTimestamp;

    private CloudSolrClient client;

    /**
     * Initialize connection to Solr instance.
     */
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

        if (defaultIdentifierTimestamp == null) {
            defaultIdentifierTimestamp = DEFAULT_IDENTIFIER_TIMESTAMP;
        }
    }

    /**
     * Perform query to the Solr instance to get the identifiers. This method will return first page of the resulting list and
     * if more than <code>identifiersPerPage</code> identifiers should be returned the resumption token will be returned as part
     * of the response object.
     *
     * @param metadataPrefix metadata prefix of identifiers
     * @param from starting date
     * @param until ending date
     * @param set set that the identifier belongs to
     * @return first or the only page of the list of identifiers
     * @throws OaiPmhException
     */
    @Override
    public ListIdentifiers listIdentifiers(String metadataPrefix, Date from, Date until, String set) throws OaiPmhException {
        LOG.info("List identifiers: from {}, until {}, set {}, metadataPrefix {}", from, until, set, metadataPrefix);

        SolrQuery query = SolrQueryBuilder.listIdentifiers(from, until, set, identifiersPerPage);
        ListIdentifiers result = listIdentifiers(query, 0, null);
        if (result.getHeaders().isEmpty()) {
            throw new NoRecordsMatchException("No records found!");
        }
        return result;
    }

    /**
     * Perform query to the Solr instance using cursor mark encoded in resumption token that is given as the input parameter.
     * If there are more identifiers to be retrieved next resumption token will be part of the response object.
     *
     * @param resumptionToken decoded resumption token used to retrieve next page of results
     * @return next or last page of the list of identifiers
     * @throws OaiPmhException
     */
    @Override
    public ListIdentifiers listIdentifiers(ResumptionToken resumptionToken) throws OaiPmhException {
        SolrQuery query = SolrQueryBuilder.listIdentifiers(resumptionToken.getFilterQuery(), resumptionToken.getValue(), identifiersPerPage);
        return listIdentifiers(query, resumptionToken.getCursor() + identifiersPerPage, resumptionToken.getValue());
    }

    /**
     * Perform the direct query on the Solr client. Query is given as the parameter. Cursor parameter is the current number of retrieved identifiers
     * and is used for preparation of the next resumption token. Previous cursor mark is used for detecting the last page of results (Solr returns the
     * same cursor mark for all queries that are done after the last page of results is retrieved).
     *
     * @param query Solr query that can be directly used with Solr client
     * @param cursor current number of retrieved identifiers
     * @param previousCursorMark cursor mark that was used for previous page
     * @return next page of the list of identifiers
     * @throws OaiPmhException
     */
    private ListIdentifiers listIdentifiers(SolrQuery query, long cursor, String previousCursorMark) throws OaiPmhException {
        try {
            QueryResponse response = client.query(query);
            return responseToListIdentifiers(response, cursor, previousCursorMark);
        } catch (SolrServerException | IOException e) {
            throw new OaiPmhException(e.getMessage());
        }
    }

    /**
     * Prepare the ListIdentifiers response object from the response received from Solr. Cursor is the current number of received
     * identifiers and is used to create next resumption token. Previous cursor mark is used to detect the last page of results for
     * which there is no next resumption token.
     *
     * @param response response retrieved from Solr
     * @param cursor current number of results
     * @param previousCursorMark cursor mark that was used for previous page
     * @return next page of the list of identifiers
     */
    private ListIdentifiers responseToListIdentifiers(QueryResponse response, long cursor, String previousCursorMark) {
        List<Header> headers = new ArrayList<>();

        SolrDocumentList docs = response.getResults();
        for (SolrDocument document : docs) {
            headers.add(documentToHeader(document));
        }

        ResumptionToken resumptionToken = prepareResumptionToken(response, cursor, previousCursorMark, docs);
        return new ListIdentifiers(headers, resumptionToken);
    }

    private ResumptionToken prepareResumptionToken(QueryResponse response, long cursor, String previousCursorMark, SolrDocumentList docs) {
        ResumptionToken resumptionToken;
        if (shouldCreateResumptionToken(response, cursor, previousCursorMark)) {
            resumptionToken = ResumptionTokenHelper.createResumptionToken(response.getNextCursorMark(),
                    docs.getNumFound(), new Date(System.currentTimeMillis() + resumptionTokenTTL), cursor, getFilterQuery(response));
        } else {
            resumptionToken = null;
        }
        return resumptionToken;
    }

    /**
     * Check whether resumption token for the next page should be created. This is based on the cursor mark that is returned
     * in the query response from Solr. When there are no more results Solr returns the same cursor mark for each request having
     * the same query. In order to reduce number of queries to Solr the last page is detected by comparing the next cursor mark
     * and the previous cursor mark and number of the retrieved identifiers.
     *
     * @param response query response retrieved from Solr
     * @param cursor current number of returned identifiers
     * @param previousCursorMark cursor mark used in the previous query
     * @return true when next resumption token should be created
     */
    private boolean shouldCreateResumptionToken(QueryResponse response, long cursor, String previousCursorMark) {
        // just one page of results
        if (previousCursorMark == null && response.getResults().size() == response.getResults().getNumFound()) {
            return false;
        }
        // last page
        return !response.getNextCursorMark().equals(previousCursorMark) && response.getResults().size() + cursor != response.getResults().getNumFound();
    }

    /**
     * Retrieve filter query part from the query response. This filter is used to create resumption token that is returned
     * as part of the response object.
     *
     * @param response query response retrieved from Solr
     * @return a list of strings corresponding to the filter query used in the Solr query
     */
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

    /**
     * Convert Solr document to the Header object that is returned for each identifier.
     *
     * @param document Solr document retrieved from the query response
     * @return header object based on the Solr document
     */
    private Header documentToHeader(SolrDocument document) {
        List<String> sets = new ArrayList<>();
        Object setNames = document.getFieldValues(DATASET_NAME);
        if (setNames != null) {
            for (Object value : document.getFieldValues(DATASET_NAME)) {
                sets.add((String) value);
            }
        }

        Date timestamp = defaultIdentifierTimestamp;
        Object timestampUpdate = document.getFieldValue(TIMESTAMP_UPDATE);
        if (timestampUpdate != null) {
            timestamp = (Date) timestampUpdate;
        }
        return new Header((String) document.getFieldValue(EUROPEANA_ID), timestamp, sets);
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
