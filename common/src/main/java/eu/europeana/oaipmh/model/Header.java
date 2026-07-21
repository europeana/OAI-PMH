package eu.europeana.oaipmh.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

/**
 * This class represents header tag in the ListIdentifiers response XML
 */
@XmlRootElement(name=header)
public class Header {

    @XmlElement(name = SerializationConstants.identifier)
    private String identifier;

    @XmlElement(name = SerializationConstants.datestamp)
    private Date datestamp;

    @XmlElement(name = SerializationConstants.setSpec)
    private List<String> setSpec;

    // empty constructor to allow deserialization
    protected Header() {}

    public Header(String identifier, Date datestamp, String setSpec) {
        this.identifier = identifier;
        this.datestamp = datestamp;
        if (this.setSpec == null) {
            this.setSpec = new ArrayList<>();
        }
        this.setSpec.add(setSpec);
    }

    public Header(String identifier, Date datestamp, List<String> setSpec) {
        this.identifier = identifier;
        this.datestamp = datestamp;
        this.setSpec = setSpec;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Date getDatestamp() {
        return datestamp;
    }

    public void setDatestamp(Date datestamp) {
        this.datestamp = datestamp;
    }

    public List<String> getSetSpec() {
        return setSpec;
    }

    public void setSetSpec(String setSpec) {
        if (this.setSpec == null) {
            this.setSpec = new ArrayList<>();
        }
        this.setSpec.add(setSpec);
    }

    public void setSetSpec(List<String> setSpec) {
        this.setSpec = setSpec;
    }

}
