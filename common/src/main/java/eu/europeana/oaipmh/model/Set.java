package eu.europeana.oaipmh.model;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * This class represents set tag in the ListSets response XML
 */
public class Set implements Serializable {

    @XmlElement
    private String setSpec;

    @XmlElement
    private String setName;

    public Set() {
        // empty constructor to allow deserialization
    }

    public Set(String setSpec, String setName) {
        this.setSpec = setSpec;
        this.setName = setName;
    }

    public String getSetSpec() {
        return setSpec;
    }

    public void setSetSpec(String setSpec) {
        this.setSpec = setSpec;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }
}
