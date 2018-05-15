package eu.europeana.oaipmh.model;

import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.ListSetsResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the ListSets tag in the ListSets verb XML response
 */
@XmlRootElement(name="ListSets")
@XmlType(propOrder={"sets", "resumptionToken"})
public class ListSets extends OAIPMHVerb {

    private static final long serialVersionUID = -8111855376100850425L;

    @XmlElement(name="set")
    private List<Set> sets;

    @XmlElement
    private ResumptionToken resumptionToken;

    public ListSets() {
        this.sets = new ArrayList<>();
    }

    public ListSets(List<Set> sets, ResumptionToken resumptionToken) {
        this.sets = sets;
        this.resumptionToken = resumptionToken;
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
    public OAIResponse getResponse(OAIRequest request) {
        return new ListSetsResponse(this, request);
    }
}
