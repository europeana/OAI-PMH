package eu.europeana.oaipmh.model.request;

import java.util.HashMap;
import java.util.Map;
import eu.europeana.oaipmh.model.serialize.SerializationHandler;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import static eu.europeana.oaipmh.model.SerializationConstants.*;

public class OAIRequestAdapter extends XmlAdapter<JsonNode, OAIRequest> {

    private static Map<String,Class<? extends OAIRequest>> map = new HashMap<>();

//    private final XmlMapper mapper ;

    public OAIRequestAdapter()  {
    }

    static {
        map.put(GetRecord          , GetRecordRequest.class);
        map.put(Identify           , IdentifyRequest.class);
        map.put(ListIdentifiers    , ListIdentifiersRequest.class);
        map.put(ListMetadataFormats, ListMetadataFormatsRequest.class);
        map.put(ListRecords        , ListRecordsRequest.class);
        map.put(ListSets           , ListSetsRequest.class);
    }

//    public OAIRequestAdapter(XmlMapper mapper) {
//        this.mapper = mapper;
//        init();
//    }

//    private void init() {
//        map.put(GetRecord          , GetRecordRequest.class);
//        map.put(Identify           , IdentifyRequest.class);
//        map.put(ListIdentifiers    , ListIdentifiersRequest.class);
//        map.put(ListMetadataFormats, ListMetadataFormatsRequest.class);
//        map.put(ListRecords        , ListRecordsRequest.class);
//        map.put(ListSets           , ListSetsRequest.class);
//    }

    @Override
    public JsonNode marshal(OAIRequest req) throws Exception {
        return SerializationHandler.getSerialization().valueToTree(req);
//        return mapper.valueToTree(req);
    }

    @Override
    public OAIRequest unmarshal(JsonNode node) throws Exception {
        Class<? extends OAIRequest> c = getVerb(node.get(verb).asText());
        return SerializationHandler.getSerialization().treeToValue(node, c);
//        return mapper.treeToValue(node, c);
    }

    private Class<? extends OAIRequest> getVerb(String verb) {
        if ( verb == null ) {
            throw new IllegalArgumentException("Unknown verb: " + verb);
        }
        Class<? extends OAIRequest> c = map.get(verb);
        if ( c == null ) {
            throw new IllegalArgumentException("Unknown verb: " + verb);
        }
        return c;
    }

}