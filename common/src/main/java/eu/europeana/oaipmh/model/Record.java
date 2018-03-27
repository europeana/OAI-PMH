package eu.europeana.oaipmh.model;

import eu.europeana.corelib.definitions.jibx.RDF;

import javax.xml.bind.annotation.XmlElement;

public class Record {
    @XmlElement
    private Header header;

    @XmlElement
    private RDF metadata;

    public Record() {
        this.metadata = null;
        this.header = null;
    }

    public Record(Header header, RDF metadata) {
        this.header = header;
        this.metadata = metadata;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public RDF getMetadata() {
        return metadata;
    }

    public void setMetadata(RDF metadata) {
        this.metadata = metadata;
    }
}
