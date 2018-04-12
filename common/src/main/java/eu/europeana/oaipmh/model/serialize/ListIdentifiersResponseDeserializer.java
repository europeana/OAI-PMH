package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.request.ListIdentifiersRequest;
import eu.europeana.oaipmh.model.response.ListIdentifiersResponse;
import eu.europeana.oaipmh.util.DateConverter;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a custom deserializer to support deserializing unwrapped lists
 */
public class ListIdentifiersResponseDeserializer extends StdDeserializer<ListIdentifiersResponse> {

    public ListIdentifiersResponseDeserializer() {
        this(null);
    }

    public ListIdentifiersResponseDeserializer(Class<?> vc) {
        super(vc);
    }

    /**
     * This method is a custom deserializer for nodes corresponding to the ListIdentifiers response. It's purpose is to correctly
     * deserialize header lists which are unwprapped.
     *
     * @param jp parser
     * @param ctxt context
     * @return correctly deserialized ListIdentifiersResponse object
     * @throws IOException
     */
    @Override
    public ListIdentifiersResponse deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        JsonNode mainNode = node.get("OAI-PMH");
        JsonNode requestNode = mainNode.get("request");
        JsonNode responseDateNode = mainNode.get("responseDate");
        JsonNode listIdentifiersNode = mainNode.get("ListIdentifiers");
        JsonNode headerNode = listIdentifiersNode.get("header");

        ListIdentifiersRequest listIdentifiersRequest = getListIdentifiersRequest(requestNode);

        ListIdentifiersResponse listIdentifiersResponse = new ListIdentifiersResponse();
        listIdentifiersResponse.setResponseDate(DateConverter.fromIsoDateTime(responseDateNode.asText()));
        listIdentifiersResponse.setRequest(listIdentifiersRequest);

        ListIdentifiers listIdentifiers = new ListIdentifiers();
        listIdentifiers.setResumptionToken(getResumptionToken(listIdentifiersNode.get("resumptionToken")));
        listIdentifiers.setHeaders(getHeaders(headerNode));

        listIdentifiersResponse.setListIdentifiers(listIdentifiers);

        return listIdentifiersResponse;
    }

    /**
     * Deserialize Header entries into the list.
     *
     * @param header header json node
     * @return list of header objects
     */
    private List<Header> getHeaders(JsonNode header) {
        List<Header> headers = new ArrayList<>();

        if (header.isArray()) {
            for (int i = 0; i < header.size(); i++) {
                JsonNode identifierNode = header.get(i);
                Header headerObject = new Header();

                // identifier should never be null, but better safe than sorry
                JsonNode id = identifierNode.get("identifier");
                if (id == null) {
                    LogManager.getLogger(ListIdentifiersResponseDeserializer.class).error("No id found in header! "+header.textValue());
                } else {
                    headerObject.setIdentifier(identifierNode.get("identifier").asText());
                }

                JsonNode dateNode = identifierNode.get("datestamp");
                if (dateNode != null) {
                    headerObject.setDatestamp(DateConverter.fromIsoDateTime(identifierNode.get("datestamp").asText()));
                }

                JsonNode setSpecNode = identifierNode.get("setSpec");
                if (setSpecNode != null) {
                    if (setSpecNode.isArray()) {
                        for (int s = 0; s < setSpecNode.size(); s++) {
                            JsonNode setSpecEntry = setSpecNode.get(s);
                            headerObject.setSetSpec(setSpecEntry.get(0).asText());
                        }
                    } else {
                        headerObject.setSetSpec(setSpecNode.asText());
                    }
                }
                headers.add(headerObject);
            }
        }
        return headers;
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
     * Prepare ListIdentifiersRequest object from the json node.
     *
     * @param request request json node
     * @return ListIdentifiersRequest object desrialized from the json node
     */
    private ListIdentifiersRequest getListIdentifiersRequest(JsonNode request) {
        ListIdentifiersRequest listIdentifiersRequest = new ListIdentifiersRequest(request.get("verb").asText(), request.get("content").asText());
        if (request.get("from") != null) {
            listIdentifiersRequest.setFrom(request.get("from").asText());
        }
        if (request.get("until") != null) {
            listIdentifiersRequest.setUntil(request.get("until").asText());
        }
        if (request.get("set") != null) {
            listIdentifiersRequest.setSet(request.get("set").asText());
        }
        if (request.get("metadataPrefix") != null) {
            listIdentifiersRequest.setMetadataPrefix(request.get("metadataPrefix").asText());
        }
        if (request.get("resumptionToken") != null) {
            listIdentifiersRequest.setResumptionToken(request.get("resumptionToken").asText());
        }
        return listIdentifiersRequest;
    }
}
