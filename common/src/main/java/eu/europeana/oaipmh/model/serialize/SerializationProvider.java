package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public interface SerializationProvider {

    public XmlMapper getSerialization();
}
