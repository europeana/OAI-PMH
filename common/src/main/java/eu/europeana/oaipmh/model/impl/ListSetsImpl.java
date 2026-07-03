package eu.europeana.oaipmh.model.impl;

import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.SerializationConstants;
import eu.europeana.oaipmh.model.Set;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class represents the ListSets tag in the ListSets verb XML response
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListSetsImpl implements ListSets {

    @XmlElement(name=SerializationConstants.set)
    private List<Set> sets;

    @XmlElement(name=SerializationConstants.resumptionToken)
    private ResumptionToken resumptionToken;

    public ListSetsImpl() {
        this.sets = new ArrayList<>();
    }

    public ListSetsImpl(List<Set> sets, ResumptionToken resumptionToken) {
        this.sets = sets;
        this.resumptionToken = resumptionToken;
    }

    @Override
    public boolean isEmpty() {
        return sets.isEmpty();
    }

    @Override
    public Stream<Set> stream() {
        return sets.stream();
    }

    public List<Set> getSets() {
        return sets;
    }

    public void setSets(List<Set> sets) {
        this.sets = sets;
    }

    public ResumptionToken getResumptionToken() { return resumptionToken; }

    public void setResumptionToken(ResumptionToken resumptionToken) { this.resumptionToken = resumptionToken; }

    @Override
    public void close() throws Exception {
    }

    public static class Adapter extends XmlAdapter<ListSetsImpl
                                                 , ListSets> {
        public ListSets unmarshal(ListSetsImpl v) { 
            return v; 
        }
        
        public ListSetsImpl marshal(ListSets v) { 
            return (ListSetsImpl)v;
        }
    }
}
