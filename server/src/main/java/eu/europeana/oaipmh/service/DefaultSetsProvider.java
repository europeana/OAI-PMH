package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.Set;
import eu.europeana.oaipmh.service.exception.InternalServerErrorException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.ResumptionTokenHelper;
import eu.europeana.oaipmh.util.SolrQueryBuilder;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.europeana.oaipmh.util.SolrConstants.DATASET_NAME;

public class DefaultSetsProvider extends SolrBasedProvider implements SetsProvider {
    @Value("${setsPerPage}")
    private int setsPerPage;

    /**
     * List the first or the only page of sets.
     *
     * @return list sets object
     * @throws OaiPmhException
     */
    @Override
    public ListSets listSets(Date from, Date until) throws OaiPmhException {
        QueryResponse response = executeQuery(SolrQueryBuilder.listSets(setsPerPage, from, until, 0));
        FieldStatsInfo info = response.getFieldStatsInfo().get(DATASET_NAME);
        if (info == null) {
            throw new InternalServerErrorException("An error occurred while retrieving information from the index.");
        }
        return responseToListSets(response, 0, info.getCountDistinct());
    }

    /**
     * List the next page of sets based on the resumption token.
     *
     * @param resumptionToken decoded resumption token used to retrieve next page of results
     * @return ListSets object with the next page of sets and possibly with resumption token if necessary
     * @throws OaiPmhException
     */
    @Override
    public ListSets listSets(ResumptionToken resumptionToken) throws OaiPmhException {
        QueryResponse response = executeQuery(SolrQueryBuilder.listSets(setsPerPage, null, null, resumptionToken.getCursor() + setsPerPage));
        return responseToListSets(response, resumptionToken.getCursor() + setsPerPage, resumptionToken.getCompleteListSize());
    }

    /**
     * Retrieve information from the Solr response and put it into the ListSets object.
     *
     * @param response response returned by Solr
     * @param cursor new cursor for the resumption token
     * @param completeListSize number of all elements
     * @return ListSets object with sets and resumption token if necessary
     */
    private ListSets responseToListSets(QueryResponse response, long cursor, long completeListSize) {
        ListSets listSets = new ListSets();
        List<Set> sets = new ArrayList<>();

        FacetField field = response.getFacetField(DATASET_NAME);
        for (FacetField.Count setCounts : field.getValues()) {
            String setName = setCounts.getName();
            // get the Set ID from edm_datasetName
            String setIdentifier = datasetNameToId(setName);
            // check if there is already a setId added in the sets List. if yes, add the setNames to the already existing set.
            // This is done to handle the parted/divided datasets. See EA-2923
            Set set = getSetIfExistsBySetId(sets, setIdentifier);
            if (set != null) {
                set.getSetName().add(setName);
            } else {
                sets.add(new Set(setIdentifier, setName));
            }
        }
        listSets.setSets(sets);

        if (shouldCreateResumptionToken(cursor, field.getValueCount(), completeListSize)) {
            // create resumption token for ListSets
            ResumptionToken resumptionToken = ResumptionTokenHelper.createResumptionToken(new Date(System.currentTimeMillis() + getResumptionTokenTTL()),
                    completeListSize,
                    cursor);
            listSets.setResumptionToken(resumptionToken);
        }
        return listSets;
    }

    /**
     * Returns the set of the matching setId
     * @param sets List of Sets
     * @param setId set Id
     * @return
     */
    private Set getSetIfExistsBySetId(List<Set> sets, String setId) {
        return sets.stream().filter(set -> set.getSetSpec().equals(setId)).findFirst().orElse(null);
    }

    /**
     * Determine if a resumption token is needed
     *
     * @param retrieved items retrieved so far
     * @param completeListSize complete list size
     * @return true when resumption token is needed
     */
    private boolean shouldCreateResumptionToken(long cursor, long retrieved, long completeListSize) {
        return cursor + retrieved < completeListSize && retrieved == setsPerPage;
    }

}
