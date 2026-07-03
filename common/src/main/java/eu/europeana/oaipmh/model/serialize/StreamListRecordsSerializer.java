package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.impl.StreamListRecords;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

public class StreamListRecordsSerializer extends JsonSerializer<StreamListRecords> {

    @Override
    public void serialize(StreamListRecords listRecords, JsonGenerator gen
                        , SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        try ( Stream<Record> stream = listRecords.stream() ) {
            JsonSerializer<Object> serRecord = provider.findValueSerializer(Record.class);
            if ( serRecord != null ) {
                Iterator<Record> iter = stream.iterator();
                while ( iter.hasNext() ) {
                    gen.writeFieldName(record);
                    serRecord.serialize(iter.next(), gen, provider);
                }
            }
        }

        writeToken(listRecords.getResumptionToken(), gen, provider);

        gen.writeEndObject();
    }

    private void writeToken(
            ResumptionToken token, JsonGenerator gen, SerializerProvider provider) 
            throws IOException {
        if ( token == null ) { return; }
        JsonSerializer<Object> serializer 
            = provider.findValueSerializer(ResumptionToken.class);
        if ( serializer == null ) { return; }
        gen.writeFieldName(resumptionToken);
        serializer.serialize(token, gen, provider);
    }
}
