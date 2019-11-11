package eu.europeana.oaipmh.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.oaipmh.model.ListSets;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

/**
 * The class represents the response for the ListSets verb request
 */
public class ListSetsResponse extends OAIResponse {

    private static final long serialVersionUID = -7718956862025421162L;

    private ListSets listSets;

    public ListSetsResponse() {}

    public ListSetsResponse(ListSets listSets, OAIRequest request) {
        super(request);
        this.listSets = listSets;
    }

    public void setListSets(ListSets listSets) {
        this.listSets = listSets;
    }

    @XmlElement(name="ListSets")
    @JacksonXmlProperty(localName = "ListSets")
    public ListSets getListSets() {
        return listSets;
    }
}
