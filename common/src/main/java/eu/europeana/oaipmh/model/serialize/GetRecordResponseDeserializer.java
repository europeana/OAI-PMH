package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.RDFMetadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.request.GetRecordRequest;
import eu.europeana.oaipmh.model.response.GetRecordResponse;
import eu.europeana.oaipmh.util.DateConverter;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

/**
 * This class is a custom deserializer to support deserializing unwrapped lists
 */
public class GetRecordResponseDeserializer extends StdDeserializer<GetRecordResponse> {

    private static final String IDENTIFIER = "identifier";
    private static final long serialVersionUID = -7632541972408801034L;

    public GetRecordResponseDeserializer() {
        this(null);
    }

    private GetRecordResponseDeserializer(Class<?> vc) {
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
    public GetRecordResponse deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        JsonNode mainNode = node.get("OAI-PMH");
        JsonNode requestNode = mainNode.get("request");
        JsonNode responseDateNode = mainNode.get("responseDate");
        // create basic response
        GetRecordRequest getRecordRequest = getGetRecordRequest(requestNode);
        GetRecordResponse getRecordResponse = new GetRecordResponse();
        getRecordResponse.setResponseDate(DateConverter.fromIsoDateTime(responseDateNode.asText()));
        getRecordResponse.setRequest(getRecordRequest);

        // check if we got error or content
        JsonNode errorNode = mainNode.get("error");
        if (errorNode != null) {
            // return basic response so harvest can continue (in case there are other sets to harvest)
            LogManager.getLogger(GetRecordResponseDeserializer.class).error("Error message: {}", errorNode);
            return getRecordResponse;
        }

        //return full response
        JsonNode getRecordNode = mainNode.get("GetRecord");
        JsonNode recordNode = getRecordNode.get("record");
        JsonNode headerNode = recordNode.get("header");

        GetRecord getRecord = new GetRecord();

        Record record = new Record();
        record.setHeader(getHeader(headerNode));
        ObjectMapper mapper = new ObjectMapper();
        RDFMetadata metadata = mapper.readValue(recordNode.get("metadata").toString(), RDFMetadata.class);
        record.setMetadata(metadata);
        getRecord.setRecord(record);

        getRecordResponse.setGetRecord(getRecord);

        return getRecordResponse;
    }

    /**
     * Deserialize Header entries into the list.
     *
     * @param header header json node
     * @return list of header objects
     */
    private Header getHeader(JsonNode header) {
        Header headerObject = new Header();

        // identifier should never be null, but better safe than sorry
        JsonNode id = header.get(IDENTIFIER);
        if (id == null) {
            if (LogManager.getLogger(GetRecordResponseDeserializer.class).isErrorEnabled()) {
                LogManager.getLogger(GetRecordResponseDeserializer.class).error("No id found in header! {}", header.textValue());
            }
        } else {
            headerObject.setIdentifier(header.get(IDENTIFIER).asText());
        }

        JsonNode dateNode = header.get("datestamp");
        if (dateNode != null) {
            headerObject.setDatestamp(DateConverter.fromIsoDateTime(header.get("datestamp").asText()));
        }

        JsonNode setSpecNode = header.get("setSpec");
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
        return headerObject;
    }

    /**
     * Prepare ListIdentifiersRequest object from the json node.
     *
     * @param request request json node
     * @return ListIdentifiersRequest object desrialized from the json node
     */
    private GetRecordRequest getGetRecordRequest(JsonNode request) {
        GetRecordRequest getRecordRequest = new GetRecordRequest(request.get("verb").asText(), request.get("content").asText());
        if (request.get(IDENTIFIER) != null) {
            getRecordRequest.setIdentifier(request.get(IDENTIFIER).asText());
        }
        if (request.get("metadataPrefix") != null) {
            getRecordRequest.setMetadataPrefix(request.get("metadataPrefix").asText());
        }
        return getRecordRequest;
    }
}
