package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.europeana.oaipmh.model.*;
import eu.europeana.oaipmh.model.request.ListSetsRequest;
import eu.europeana.oaipmh.model.response.ListSetsResponse;
import eu.europeana.oaipmh.util.DateConverter;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is a custom deserializer to support deserializing unwrapped lists
 */
public class ListSetsResponseDeserializer extends StdDeserializer<ListSetsResponse> {

    private static final String RESUMPTION_TOKEN = "resumptionToken";
    private static final long serialVersionUID = -1513167521638460815L;

    public ListSetsResponseDeserializer() {
        this(null);
    }

    public ListSetsResponseDeserializer(Class<?> vc) {
        super(vc);
    }

    /**
     * This method is a custom deserializer for nodes corresponding to the ListSet response. It's purpose is to correctly
     * deserialize sets lists which are unwprapped.
     *
     * @param jp   parser
     * @param ctxt context
     * @return correctly deserialized ListSetResponse object
     * @throws IOException
     */
    @Override
    public ListSetsResponse deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        JsonNode mainNode = node.get("OAI-PMH");
        JsonNode requestNode = mainNode.get("request");
        JsonNode responseDateNode = mainNode.get("responseDate");
        // create basic response
        ListSetsRequest listSetsRequest = getListSetRequest(requestNode);
        ListSetsResponse listSetsResponse = new ListSetsResponse();
        listSetsResponse.setResponseDate(DateConverter.fromIsoDateTime(responseDateNode.asText()));
        listSetsResponse.setRequest(listSetsRequest);

        // check if we got error or content
        JsonNode errorNode = mainNode.get("error");
        if (errorNode != null) {
            // return basic response so harvest can continue (in case there are other sets to harvest)
            LogManager.getLogger(ListSetsResponseDeserializer.class).error("Error message: {}", errorNode);
            return listSetsResponse;
        }

        //return full response
        JsonNode listSetNode = mainNode.get("ListSets");
        JsonNode setNode = listSetNode.get("set");

        ListSets listSets = new ListSets();
        listSets.setResumptionToken(getResumptionToken(listSetNode.get(RESUMPTION_TOKEN)));
        listSets.setSets(getSets(setNode));

        listSetsResponse.setListSets(listSets);

        return listSetsResponse;
    }

    /**
     * Deserialize set entries into the list.
     *
     * @param set json node
     * @return list of set objects
     */
    public List<Set> getSets(JsonNode set) {
        List<Set> sets = new ArrayList<>();

        if (set.isArray()) {
            for (int i = 0; i < set.size(); i++) {
                JsonNode setNode = set.get(i);
                Set setObject = new Set();

                JsonNode setName = setNode.get("setName");
                setSetName(setNode, setObject, setName);

                JsonNode setSpec = setNode.get("setSpec");
                setSetSpec(setNode, setObject, setSpec);

                sets.add(setObject);
            }
        }
        return sets;
    }

    private void setSetName(JsonNode setNode, Set setObject, JsonNode setNameNode) {
        List<String> setName = new ArrayList<>();
        if (setNameNode.isArray()) {
            for (int i = 0; i < setNameNode.size(); i++) {
                setName.add(setNameNode.get(i).asText());
            }
            setObject.setSetName(setName);
        } else {
                setObject.setSetName(Collections.singletonList(setNode.get("setName").asText()));
        }
    }

    private void setSetSpec(JsonNode setNode, Set setObject, JsonNode setNameNode) {
        if (setNameNode != null) {
            setObject.setSetSpec(setNode.get("setSpec").asText());
        }
    }

    /**
     * Deserialize resumption token node.
     *
     * @param resumptionTokenNode resumption token json node
     * @return resumption token object deserialized from the json node
     */
    private ResumptionToken getResumptionToken(JsonNode resumptionTokenNode) {
        if (resumptionTokenNode != null) {
            ResumptionToken token = new ResumptionToken();
            token.setValue(resumptionTokenNode.get("content").asText());
            token.setExpirationDate(DateConverter.fromIsoDateTime(resumptionTokenNode.get("expirationDate").asText()));
            token.setCursor(resumptionTokenNode.get("cursor").asLong());
            token.setCompleteListSize(resumptionTokenNode.get("completeListSize").asLong());
            return token;
        }
        return null;
    }

    /**
     * Prepare ListSetsRequest object from the json node.
     *
     * @param request request json node
     * @return ListSetsRequest object desrialized from the json node
     */
    private ListSetsRequest getListSetRequest(JsonNode request) {
        ListSetsRequest listSetsRequest = new ListSetsRequest(request.get("verb").asText(), request.get("content").asText());
        if (request.get(RESUMPTION_TOKEN) != null) {
            listSetsRequest.setResumptionToken(request.get(RESUMPTION_TOKEN).asText());
        }
        return listSetsRequest;
    }
}
