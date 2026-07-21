package eu.europeana.oaipmh.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

@XmlRootElement(name=record)
public class Record {

    @XmlElement(name=SerializationConstants.header)
    private Header header;

    @XmlElement(name=SerializationConstants.metadata)
    private Metadata metadata;

    protected Record() {
        this.metadata = null;
        this.header = null;
    }

    public Record(Header header, Metadata metadata) {
        this.header = header;
        this.metadata = metadata;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
