package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.OAIError;
import eu.europeana.oaipmh.model.OAIPMHVerb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static eu.europeana.oaipmh.model.SerializationConstants.*;


public class VerbDeserializer extends JsonDeserializer<OAIPMHVerb> {

    private static Map<String,Class<? extends OAIPMHVerb>> map = new HashMap<>();

    static {
        map.put(GetRecord          , GetRecord.class);
        map.put(Identify           , Identify.class);
        map.put(ListIdentifiers    , ListIdentifiers.class);
        map.put(ListMetadataFormats, ListMetadataFormats.class);
        map.put(ListRecords        , ListRecords.class);
        map.put(ListSets           , ListSets.class);
        map.put(error              , OAIError.class);
    }

    @Override
    public OAIPMHVerb deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        Class<? extends OAIPMHVerb> c = map.get(jp.currentName());
        return ctxt.readValue(jp, c);
    }
}
