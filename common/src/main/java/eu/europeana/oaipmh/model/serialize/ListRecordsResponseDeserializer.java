package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.europeana.oaipmh.model.*;
import eu.europeana.oaipmh.model.request.ListRecordsRequest;
import eu.europeana.oaipmh.model.response.ListRecordsResponse;
import eu.europeana.oaipmh.util.DateConverter;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a custom deserializer to support deserializing unwrapped lists
 */
public class ListRecordsResponseDeserializer extends StdDeserializer<ListRecordsResponse> {

    private static final String RESUMPTION_TOKEN = "resumptionToken";

    public ListRecordsResponseDeserializer() {
        this(null);
    }

    public ListRecordsResponseDeserializer(Class<?> vc) {
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
    public ListRecordsResponse deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        JsonNode mainNode = node.get("OAI-PMH");
        JsonNode requestNode = mainNode.get("request");
        JsonNode responseDateNode = mainNode.get("responseDate");
        // create basic response
        ListRecordsRequest listRecordsRequest = getListRecordsRequest(requestNode);
        ListRecordsResponse listRecordsResponse = new ListRecordsResponse();
        listRecordsResponse.setResponseDate(DateConverter.fromIsoDateTime(responseDateNode.asText()));
        listRecordsResponse.setRequest(listRecordsRequest);

        // check if we got error or content
        JsonNode errorNode = mainNode.get("error");
        if (errorNode != null) {
            // return basic response so harvest can continue (in case there are other sets to harvest)
            LogManager.getLogger(ListRecordsResponseDeserializer.class).error("Error message: {}", errorNode);
            return listRecordsResponse;
        }

        //return full response
        JsonNode listRecordsNode = mainNode.get("ListRecords");
        JsonNode recordNode = listRecordsNode.get("record");

        ListRecords listRecords = new ListRecords();
        listRecords.setResumptionToken(getResumptionToken(listRecordsNode.get(RESUMPTION_TOKEN)));
        listRecords.setRecords(getRecords(recordNode));

        listRecordsResponse.setListRecords(listRecords);

        return listRecordsResponse;
    }

    /**
     * Deserialize Record entries into the list.
     *
     * @param node record json node
     * @return list of Record objects
     */
    private List<Record> getRecords(JsonNode node) throws IOException {
        List<Record> records = new ArrayList<>();

        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                records.add(getRecord(node.get(i)));
            }
        } else {
            records.add(getRecord(node));
        }
        return records;
    }

    private Record getRecord(JsonNode recordNode) throws IOException {
        if (recordNode != null) {
            Record record = new Record();
            record.setHeader(getHeader(recordNode.get("header")));
            ObjectMapper mapper = new ObjectMapper();
            RDFMetadata metadata = mapper.readValue(recordNode.get("metadata").toString(), RDFMetadata.class);
            record.setMetadata(metadata);
            return record;
        }
        return null;
    }

    private Header getHeader(JsonNode headerNode) {
        Header headerObject = new Header();

        // identifier should never be null, but better safe than sorry
        JsonNode id = headerNode.get("identifier");
        setIdentifier(headerNode, headerObject, id);

        JsonNode dateNode = headerNode.get("datestamp");
        setDatestamp(headerNode, headerObject, dateNode);

        JsonNode setSpecNode = headerNode.get("setSpec");
        setSetSpec(headerObject, setSpecNode);
        return headerObject;
    }

    private void setSetSpec(Header headerObject, JsonNode setSpecNode) {
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
    }

    private void setDatestamp(JsonNode headerNode, Header headerObject, JsonNode dateNode) {
        if (dateNode != null) {
            headerObject.setDatestamp(DateConverter.fromIsoDateTime(headerNode.get("datestamp").asText()));
        }
    }

    private void setIdentifier(JsonNode headerNode, Header headerObject, JsonNode id) {
        if (id == null) {
            LogManager.getLogger(ListRecordsResponseDeserializer.class).error("No id found in header! ");
        } else {
            headerObject.setIdentifier(headerNode.get("identifier").asText());
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
     * Prepare ListRecordsRequest object from the json node.
     *
     * @param request request json node
     * @return ListRecordsRequest object desrialized from the json node
     */
    private ListRecordsRequest getListRecordsRequest(JsonNode request) {
        ListRecordsRequest listRecordsRequest = new ListRecordsRequest(request.get("verb").asText(), request.get("content").asText());
        if (request.get("from") != null) {
            listRecordsRequest.setFrom(request.get("from").asText());
        }
        if (request.get("until") != null) {
            listRecordsRequest.setUntil(request.get("until").asText());
        }
        if (request.get("set") != null) {
            listRecordsRequest.setSet(request.get("set").asText());
        }
        if (request.get("metadataPrefix") != null) {
            listRecordsRequest.setMetadataPrefix(request.get("metadataPrefix").asText());
        }
        if (request.get(RESUMPTION_TOKEN) != null) {
            listRecordsRequest.setResumptionToken(request.get(RESUMPTION_TOKEN).asText());
        }
        return listRecordsRequest;
    }
}
