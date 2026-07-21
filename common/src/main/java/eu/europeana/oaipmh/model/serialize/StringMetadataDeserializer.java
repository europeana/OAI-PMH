package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;

import eu.europeana.oaipmh.model.Metadata;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

public class StringMetadataDeserializer extends JsonDeserializer<Metadata> {

    public StringMetadataDeserializer() {
    }

    @Override
    public Metadata deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

        if (!(jp instanceof FromXmlParser)) {
            throw new IOException("Parser is not an instance of FromXmlParser. Unable to extract DOM.");
        }

        XMLStreamReader staxReader = ((FromXmlParser)jp).getStaxReader();
        try {
            JsonLocation l1 = jp.getCurrentLocation();
            // 3. Pipeline the StAX token stream directly into the DOM Document
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            // This consumes ONLY the current nested element block from the stream safely
            transformer.transform(new StAXSource(staxReader), new StreamResult(writer));

            jp.clearCurrentToken();
            scan(jp, JsonToken.END_OBJECT); //end rdf:RDF
//            scan(jp, JsonToken.END_OBJECT); //end metadata
            // 4. Return the parsed root element containing intact attributes/namespaces
            return new Metadata(writer.toString());

        } catch (Exception e) {
            throw new IOException("Failed to extract raw StAX token block into DOM Element", e);
        }
    }

    private void scan(JsonParser jp, JsonToken until) throws IOException {
        while (jp.nextToken() != null) {
            if (jp.getCurrentToken() == until ) { break; }
        }
    }
}
