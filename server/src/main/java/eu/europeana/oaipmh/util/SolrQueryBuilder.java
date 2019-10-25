package eu.europeana.oaipmh.util;

import org.apache.solr.client.solrj.SolrQuery;

import java.util.Date;

import static eu.europeana.oaipmh.util.SolrConstants.*;
import static org.apache.solr.common.params.CursorMarkParams.CURSOR_MARK_PARAM;
import static org.apache.solr.common.params.FacetParams.FACET_OFFSET;
import static org.apache.solr.common.params.StatsParams.STATS;
import static org.apache.solr.common.params.StatsParams.STATS_CALC_DISTINCT;

/**
 * The helper class used to build Solr queries based on the given filter parameters.
 *
 */
public class SolrQueryBuilder {

    private static final String FQ_TEMPLATE = "%s:%s_*";

    private static final String DATE_RANGE_TEMPLATE = "%s:[%s TO %s]";

    private static final String START_CURSOR = "*";

    private static final String ANY_DATE = "*";

    private SolrQueryBuilder() {}


    /**
     * Create Solr query using the cursor mark and other parameters necessary to add filter queries
     * corresponding to the given cursor mark.
     *
     * @param from staring date
     * @param until ending date
     * @param set set that the identifiers belong to
     * @param cursorMark cursor mark used for paging the results
     * @param rows number of results to retrieve
     * @return prepared query
     */
    public static SolrQuery listIdentifiers(Date from, Date until, String set, String cursorMark, int rows) {
        if (cursorMark == null || cursorMark.isEmpty()) {
            cursorMark = START_CURSOR;
        }
        SolrQuery query = getDefaultListIdentifiersSolrQuery(rows, cursorMark);
        addFilters(query, set,  from,  until);
        return query;
    }

    /**
     * Add filter query part to the Solr query.
     *
     * @param query Solr query
     * @param set set identifier that the identifiers belong to
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

    /**
     * Prepare Solr query used for listing sets. Limit and offset are needed to returned paged facets with dataset names.
     * When offset is 0 (meaning that this is the first page of results) the statistics for dataset name field are computed to determine
     * number of all facet values.
     *
     * @param limit items per page
     * @param offset start from specific facet
     * @return Solr query used for listing sets
     */
    public static SolrQuery listSets(int limit, long offset) {
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(0);
        query.setFields(DATASET_NAME);
        query.addFacetField(DATASET_NAME);
        query.setFacet(true);
        query.setFacetLimit(limit);
        query.setFacetMinCount(1);
        if (offset > 0) {
            query.setParam(FACET_OFFSET, String.valueOf(offset));
        } else {
            query.set(STATS, true);
            query.setGetFieldStatistics(DATASET_NAME);
            query.set(STATS_CALC_DISTINCT, true);
        }
        return query;
    }

    /**
     * Prepare Solr query used to retrieve the earliest timestamp in timestamp_update field.
     *
     * @return Solr query for earliest timestamp
     */
    public static SolrQuery earliestTimestamp() {
        SolrQuery query = new SolrQuery("*:*");
        query.setRows(1);
        query.setFields(TIMESTAMP_UPDATE);
        query.setSort(TIMESTAMP_UPDATE, SolrQuery.ORDER.asc);
        query.setParam(WT_PARAM, WT_JSON);
        return query;
    }
}
