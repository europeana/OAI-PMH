package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.OAIError;
import eu.europeana.oaipmh.model.OAIPMHVerb;
import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.OAIResponse;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

/*
 * This class handles the serialization of OAIResponses. It is necessary to
 * handle the polymorphism of the verbs.
 * 
 * Ideally, a VerbSerializer would be the most intuitive way to implement 
 * it but, when using Jackson XML, it is necessary because of the way it 
 * handles XML as JSON
 */
public class OAIResponseSerializer extends JsonSerializer<OAIResponse> {

    private static Map<Class<? extends OAIPMHVerb>,String> map = new HashMap<>();

    static {
        map.put(GetRecord.class          , GetRecord);
        map.put(Identify.class           , Identify);
        map.put(ListIdentifiers.class    , ListIdentifiers);
        map.put(ListMetadataFormats.class, ListMetadataFormats);
        map.put(ListRecords.class        , ListRecords);
        map.put(ListSets.class           , ListSets);
        map.put(OAIError.class           , error);
    }

    @Override
    public void serialize(OAIResponse value, JsonGenerator gen
                        , SerializerProvider provider) throws IOException {

        gen.writeStartObject();
        if (gen instanceof ToXmlGenerator) {
            ToXmlGenerator xmlGen = (ToXmlGenerator) gen;

            xmlGen.setNextIsAttribute(true);
            xmlGen.writeStringField(XMLNS, OAI_NS);

            xmlGen.setNextIsAttribute(true);
            xmlGen.writeStringField(XMLNS_XSI, XSI_NS);

            xmlGen.setNextIsAttribute(true);
            xmlGen.writeStringField(XSI_LOCATION, OAI_LOCATION);

            xmlGen.setNextIsAttribute(false);
        }

        gen.writeFieldName("responseDate");
        Date date = value.getResponseDate();
        // 3. Fetch Jackson's default serializer for the concrete sub-class
        provider.findValueSerializer(date.getClass()).serialize(date, gen, provider);

        gen.writeFieldName("request");
        OAIRequest req = value.getRequest();
        provider.findValueSerializer(req.getClass()).serialize(req, gen, provider); // problem here

        OAIPMHVerb verb = value.getVerb();
        gen.writeFieldName(getTag(verb));
        provider.findValueSerializer(verb.getClass()).serialize(verb, gen, provider);

        gen.writeEndObject();
        
    }

    private String getTag(OAIPMHVerb verb) {
        Class<?> c = verb.getClass();
        for ( Map.Entry<Class<? extends OAIPMHVerb>,String> entry : map.entrySet() ) {
            if ( !entry.getKey().isAssignableFrom(c) ) { continue; }
            return entry.getValue();
        }
        return null;
    }
}
