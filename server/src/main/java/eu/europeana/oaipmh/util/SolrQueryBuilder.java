package eu.europeana.oaipmh.util;

import org.apache.solr.client.solrj.SolrQuery;

import java.util.Date;
import java.util.List;

import static eu.europeana.oaipmh.util.SolrConstants.*;
import static org.apache.solr.common.params.CursorMarkParams.CURSOR_MARK_PARAM;

/**
 * The helper class used to build Solr queries based on the given filter parameters.
 *
 */
public class SolrQueryBuilder {

    private static final String FQ_TEMPLATE = "%s:\"%s\"";

    private static final String DATE_RANGE_TEMPLATE = "%s:[%s TO %s]";

    private static final String START_CURSOR = "*";

    private static final String ANY_DATE = "*";

    /**
     * Build Solr query using from, until and set filter parameters. Rows parameter specifies number of results to be retrieved in a single query.
     *
     * @param from staring date
     * @param until ending date
     * @param set set that the identifiers belong to
     * @param rows number of results to retrieve
     * @return Solr query object that can be used with the solr client
     */
    public static SolrQuery listIdentifiers(Date from, Date until, String set, int rows) {
        SolrQuery query = getDefaultListIdentifiersSolrQuery(rows, START_CURSOR);
        addFilters(query, set,  from,  until);
        return query;
    }

    /**
     * Add filter query part to the Solr query.
     *
     * @param query Solr query
     * @param set set that the identifiers belong to
     * @param from staring date
     * @param until ending date
     */
    private static void addFilters(SolrQuery query, String set, Date from, Date until) {
        if (set != null && !set.isEmpty()) {
            query.addFilterQuery(String.format(FQ_TEMPLATE, DATASET_NAME, set));
        }

        if (from != null || until != null) {
            String fromString = from == null ? ANY_DATE : DateConverter.toIsoDate(from);
            String untilString = until == null ? ANY_DATE : DateConverter.toIsoDate(until);

            query.addFilterQuery(String.format(DATE_RANGE_TEMPLATE, TIMESTAMP_UPDATE, fromString, untilString));
        }
    }

    /**
     * Add ready to use filters to the Solr query.
     *
     * @param query query object
     * @param filterQuery list of filter query string
     */
    private static void addFilters(SolrQuery query, List<String> filterQuery) {
        for (String filter : filterQuery) {
            query.addFilterQuery(filter);
        }
    }

    /**
     * Create Solr query using filter queries and the cursor mark.
     *
     * @param filterQuery list of filter query string
     * @param cursorMark cursor mark used for paging the results
     * @param rows number of results to retrieve
     * @return prepared query
     */
    public static SolrQuery listIdentifiers(List<String> filterQuery, String cursorMark, int rows) {
        SolrQuery query = getDefaultListIdentifiersSolrQuery(rows, cursorMark);
        addFilters(query, filterQuery);
        return query;
    }

    /**
     * Prepare common Solr query object that is used for each request.
     *
     * @param rows number of results to retrieve
     * @param cursorMark cursor mark used for paging the results
     * @return base Solr query
     */
    private static SolrQuery getDefaultListIdentifiersSolrQuery(int rows, String cursorMark) {
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(rows);
        query.addField(TIMESTAMP_UPDATE);
        query.addField(EUROPEANA_ID);
        query.addField(DATASET_NAME);
        query.addSort(TIMESTAMP_UPDATE, SolrQuery.ORDER.asc);
        query.addSort(EUROPEANA_ID, SolrQuery.ORDER.asc);
        query.set(CURSOR_MARK_PARAM, cursorMark);
        query.setParam(WT_PARAM, WT_JSON);
        return query;
    }
}
