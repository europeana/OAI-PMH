package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class SerializationHandler {

    private static XmlMapper def = null;

    public static void register(SerializationProvider provider) {
        def = provider.getSerialization();
    }

    public static XmlMapper getSerialization() {
        return def;
    }
}
