package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europeana.oaipmh.model.serialize.RDFMetadataSerializer;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class Record implements Serializable {

    private static final long serialVersionUID = 4269209744172447773L;

    @XmlElement
    private Header header;

    @JsonSerialize(using = RDFMetadataSerializer.class)
    private RDFMetadata metadata;

    public Record() {
        this.metadata = null;
        this.header = null;
    }

    public Record(Header header, RDFMetadata metadata) {
        this.header = header;
        this.metadata = metadata;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public RDFMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(RDFMetadata metadata) {
        this.metadata = metadata;
    }
}
