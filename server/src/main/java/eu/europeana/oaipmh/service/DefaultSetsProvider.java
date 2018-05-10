package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.Set;
import eu.europeana.oaipmh.service.exception.InternalServerErrorException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.util.ResumptionTokenHelper;
import eu.europeana.oaipmh.util.SolrQueryBuilder;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public ListSets listSets() throws OaiPmhException {
        QueryResponse response = executeQuery(SolrQueryBuilder.listSets(setsPerPage, 0));
        FieldStatsInfo info = response.getFieldStatsInfo().get(DATASET_NAME);
        if (info == null) {
            throw new InternalServerErrorException("An error occurred while retrieving information from the index.");
        }
        return responseToListSets(response, 0, info.getCountDistinct());
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
            String setIdentifier = getSetIdentifier(setName);
            sets.add(new Set(setIdentifier, setName));
        }
        listSets.setSets(sets);

        if (shouldCreateResumptionToken(cursor, field.getValueCount(), completeListSize)) {
            // create resumption token for ListSets
            listSets.setResumptionToken(ResumptionTokenHelper.createSimpleResumptionToken(completeListSize, new Date(System.currentTimeMillis() + getResumptionTokenTTL()), cursor));
        }
        return listSets;
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

    /**
     * Retrieve set identifier from the set name. Set name always starts with identifier concatenated with the rest of the name using "_"
     *
     * @param setName name of the set
     * @return set identifier
     */
    private String getSetIdentifier(String setName) {
        int index = setName.indexOf("_");
        if (index == -1) {
            return setName;
        }
        return setName.substring(0, index);
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
        QueryResponse response = executeQuery(SolrQueryBuilder.listSets(setsPerPage, resumptionToken.getCursor() + setsPerPage));
        Path path = Paths.get("/home/helin/work/" + "listSets" + System.currentTimeMillis());
        try {
            Files.write(path, SolrResponse.serializable(response));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseToListSets(response, resumptionToken.getCursor() + setsPerPage, resumptionToken.getCompleteListSize());
    }
}
