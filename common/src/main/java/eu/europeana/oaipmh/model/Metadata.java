package eu.europeana.oaipmh.model;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name=metadata)
public class Metadata {

    private Object metadata;

    public Metadata() {}

    public Metadata(Object metadata) {
        this.metadata = metadata;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }
}
