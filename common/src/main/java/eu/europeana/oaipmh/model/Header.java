package eu.europeana.oaipmh.model;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class represents header tag in the ListIdentifiers response XML
 */
public class Header implements Serializable {

    private static final long serialVersionUID = 4303710121544438193L;

    @XmlElement
    private String identifier;

    @XmlElement
    private Date datestamp;

    @XmlElement
    private List<String> setSpec;

    public Header() {
        // empty constructor to allow deserialization
    }

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
