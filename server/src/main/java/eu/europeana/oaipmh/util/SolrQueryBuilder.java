package eu.europeana.oaipmh.util;

import org.apache.solr.client.solrj.SolrQuery;

import java.util.Date;
import java.util.List;

import static eu.europeana.oaipmh.util.SolrConstants.*;
import static org.apache.solr.common.params.CursorMarkParams.CURSOR_MARK_PARAM;

public class SolrQueryBuilder {

    private static final String FQ_TEMPLATE = "%s:\"%s\"";

    private static final String DATE_RANGE_TEMPLATE = "%s:[%s TO %s]";

    private static final String START_CURSOR = "*";

    private static final String ANY_DATE = "*";

    public static SolrQuery listIdentifiers(Date from, Date until, String set, int rows) {
        SolrQuery query = getDefaultListIdentifiersSolrQuery(rows, START_CURSOR);
        addFilters(query, set,  from,  until);
        return query;
    }

    private static void addFilters(SolrQuery query, String set, Date from, Date until) {
        if (set != null) {
            query.addFilterQuery(String.format(FQ_TEMPLATE, DATASET_NAME, set));
        }

        if (from != null || until != null) {
            String fromString = from == null ? ANY_DATE : DateConverter.toIsoDate(from);
            String untilString = until == null ? ANY_DATE : DateConverter.toIsoDate(until);

            query.addFilterQuery(String.format(DATE_RANGE_TEMPLATE, TIMESTAMP_UPDATE, fromString, untilString));
        }
    }

    private static void addFilters(SolrQuery query, List<String> filterQuery) {
        for (String filter : filterQuery) {
            query.addFilterQuery(filter);
        }
    }

    public static SolrQuery listIdentifiers(List<String> filterQuery, String cursorMark, int rows) {
        SolrQuery query = getDefaultListIdentifiersSolrQuery(rows, cursorMark);
        addFilters(query, filterQuery);
        return query;
    }

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
