package eu.europeana.oaipmh.model;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents set tag in the ListSets response XML
 */
public class Set implements Serializable {

    private static final long serialVersionUID = -8390878458067204309L;

    @XmlElement
    private String setSpec;

    @XmlElement
    private List<String> setName;

    public Set() {
        // empty constructor to allow deserialization
    }

    public Set(String setSpec, String setName) {
        this.setSpec = setSpec;
        if (this.setName == null) {
            this.setName = new ArrayList<>();
        }
        this.setName.add(setName);
    }

    public Set(String setSpec, List<String> setName) {
        this.setSpec = setSpec;
        this.setName = setName;
    }

    public String getSetSpec() {
        return setSpec;
    }

    public void setSetSpec(String setSpec) {
        this.setSpec = setSpec;
    }

    public List<String> getSetName() {
        return setName;
    }

    public void setSetName(List<String> setName) {
        this.setName = setName;
    }
}
