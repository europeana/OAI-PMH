package eu.europeana.oaipmh.model;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * This class represents set tag in the ListSets response XML
 */
@XmlRootElement(name=set)
public class Set {

    @XmlElement(name=SerializationConstants.setSpec)
    private String setSpec;

    @XmlElement(name=SerializationConstants.setName)
    private String setName;

    // empty constructor to allow deserialization
    protected Set() {}

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
