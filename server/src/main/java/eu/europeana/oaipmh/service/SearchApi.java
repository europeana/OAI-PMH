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
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static eu.europeana.oaipmh.util.SolrConstants.*;

/**
 * Retrieve information from Search API
 */
public class SearchApi extends SolrBasedProvider implements IdentifierProvider {

    private static final Logger LOG = LogManager.getLogger(SearchApi.class);

    private static final Date DEFAULT_IDENTIFIER_TIMESTAMP = DateConverter.fromIsoDateTime("1970-01-01T00:00:00Z");

    @Value("${identifiersPerPage}")
    private int identifiersPerPage;

    @Value("#{T(eu.europeana.oaipmh.util.DateConverter).fromIsoDateTime('${defaultIdentifierTimestamp}')}")
    private Date defaultIdentifierTimestamp;

    /**
     * Initialize default timestamp.
     */
    @PostConstruct
    private void initDefaults() {
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("List identifiers: from {}, until {}, set {}, metadataPrefix {}", from, until, set, metadataPrefix);
        }
        return listIdentifiers(metadataPrefix, from, until, set, 0, null);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("List identifiers: from {}, until {}, set {}, metadataPrefix {}", ResumptionTokenHelper.getFrom(resumptionToken.getValue()), DateConverter.fromIsoDateTime(ResumptionTokenHelper.getUntil(resumptionToken.getValue())), ResumptionTokenHelper.getSet(resumptionToken.getValue()), ResumptionTokenHelper.getFormat(resumptionToken.getValue()));
        }
        return listIdentifiers(ResumptionTokenHelper.getFormat(resumptionToken.getValue()),
                DateConverter.fromIsoDateTime(ResumptionTokenHelper.getFrom(resumptionToken.getValue())),
                DateConverter.fromIsoDateTime(ResumptionTokenHelper.getUntil(resumptionToken.getValue())),
                ResumptionTokenHelper.getSet(resumptionToken.getValue()),
                resumptionToken.getCursor() + identifiersPerPage, ResumptionTokenHelper.getCursorMark(resumptionToken.getValue()));
    }

    /**
     * This method does the actual work for retrieving the identifiers. It creates a proper Solr query, runs it and prepares the results.
     *
     * @param metadataPrefix metadata prefix for the identifiers
     * @param from start date
     * @param until end date
     * @param set set identifier
     * @param cursor number of items already retrieved
     * @param previousCursorMark previous cursor mark if there was any
     * @return ListIdentifiers object containing the results
     * @throws OaiPmhException
     */
    private ListIdentifiers listIdentifiers(String metadataPrefix, Date from, Date until, String set, long cursor, String previousCursorMark) throws OaiPmhException {
        QueryResponse response = executeQuery(SolrQueryBuilder.listIdentifiers(from, until, set, previousCursorMark, identifiersPerPage));
        ListIdentifiers result = responseToListIdentifiers(response);
        if (result.getHeaders().isEmpty()) {
            throw new NoRecordsMatchException("No records found!");
        }

        if (shouldCreateResumptionToken(response, cursor, previousCursorMark)) {
            ResumptionToken resumptionToken = ResumptionTokenHelper.createResumptionToken(DateConverter.toIsoDate(from),
                    DateConverter.toIsoDate(until),
                    set,
                    metadataPrefix,
                    new Date(System.currentTimeMillis() + getResumptionTokenTTL()),
                    response.getResults().getNumFound(),
                    cursor,
                    response.getNextCursorMark());
            result.setResumptionToken(resumptionToken);
        }

        return result;
    }

    /**
     * Prepare the ListIdentifiers response object from the response received from Solr. Cursor is the current number of received
     * identifiers and is used to create next resumption token. Previous cursor mark is used to detect the last page of results for
     * which there is no next resumption token.
     *
     * @param response response retrieved from Solr
     * @return next page of the list of identifiers
     */
    private ListIdentifiers responseToListIdentifiers(QueryResponse response) {
        List<Header> headers = new ArrayList<>();

        SolrDocumentList docs = response.getResults();
        for (SolrDocument document : docs) {
            headers.add(documentToHeader(document));
        }
        ListIdentifiers listIdentifiersResult = new ListIdentifiers();
        listIdentifiersResult.setHeaders(headers);
        return listIdentifiersResult;
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
     * Convert Solr document to the Header object that is returned for each identifier.
     *
     * @param document Solr document retrieved from the query response
     * @return header object based on the Solr document
     */
    private Header documentToHeader(SolrDocument document) {
        List<String> sets = new ArrayList<>();
        Collection<Object> setNames = document.getFieldValues(DATASET_NAME);
        if (setNames != null) {
            for (Object value : setNames) {
                sets.add((String) value);
            }
        }
        Date timestampUpdate = (Date) document.getFieldValue(TIMESTAMP_UPDATE);
        if (timestampUpdate == null) {
            timestampUpdate = defaultIdentifierTimestamp;
        }
        return new Header(prepareFullId((String) document.getFieldValue(EUROPEANA_ID)), timestampUpdate, sets);
    }

}
