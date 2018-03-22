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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListIdentifiersResponseDeserializer extends StdDeserializer<ListIdentifiersResponse> {

    public ListIdentifiersResponseDeserializer() {
        this(null);
    }

    public ListIdentifiersResponseDeserializer(Class<?> vc) {
        super(vc);
    }

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

    private List<Header> getHeaders(JsonNode header) {
        List<Header> headers = new ArrayList<>();

        if (header.isArray()) {
            for (int i = 0; i < header.size(); i++) {
                JsonNode identifierNode = header.get(i);
                Header headerObject = new Header();
                headerObject.setDatestamp(DateConverter.fromIsoDateTime(identifierNode.get("datestamp").asText()));
                headerObject.setIdentifier(identifierNode.get("identifier").asText());
                JsonNode setSpecNode = identifierNode.get("setSpec");
                if (setSpecNode.isArray()) {
                    for (int s = 0; s < setSpecNode.size(); s++) {
                        JsonNode setSpecEntry = setSpecNode.get(s);
                        headerObject.setSetSpec(setSpecEntry.get(0).asText());
                    }
                } else {
                    headerObject.setSetSpec(setSpecNode.asText());
                }
                headers.add(headerObject);
            }
        }
        return headers;
    }

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
