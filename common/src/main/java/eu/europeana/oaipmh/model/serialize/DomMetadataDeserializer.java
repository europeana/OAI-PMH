package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;

import eu.europeana.oaipmh.model.Metadata;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.w3c.dom.Document;

public class DomMetadataDeserializer extends JsonDeserializer<Metadata> {

    public DomMetadataDeserializer() {
    }

    @Override
    public Metadata deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

        if (!(jp instanceof FromXmlParser)) {
            throw new IOException("Parser is not an instance of FromXmlParser. Unable to extract DOM.");
        }

        FromXmlParser xmlParser = (FromXmlParser) jp;
        XMLStreamReader staxReader = xmlParser.getStaxReader();

        try {
            // 2. Create a clean, blank W3C DOM document structure
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // CRITICAL: Tells the factory to look for namespaces
            Document doc = factory.newDocumentBuilder().newDocument();

            // 3. Pipeline the StAX token stream directly into the DOM Document
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMResult result = new DOMResult(doc);
            
            // This consumes ONLY the current nested element block from the stream safely
            transformer.transform(new StAXSource(staxReader), result);

            // 4. Return the parsed root element containing intact attributes/namespaces
            return new Metadata(doc.getDocumentElement());

        } catch (Exception e) {
            throw new IOException("Failed to extract raw StAX token block into DOM Element", e);
        }
    }
}
