package eu.europeana.oaipmh.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

/**
 * The class represents the response for the ListRecords verb request
 */
public class ListRecordsResponse extends OAIResponse {

    private static final long serialVersionUID = -447267674059893746L;

    private ListRecords listRecords;

    public ListRecordsResponse() {}

    public ListRecordsResponse(ListRecords listRecords, OAIRequest request) {
        super(request);
        this.listRecords = listRecords;
    }

    public void setListRecords(ListRecords listRecords) {
        this.listRecords = listRecords;
    }

    @XmlElement(name="ListRecords")
    @JacksonXmlProperty(localName = "ListRecords")
    public ListRecords getListRecords() {
        return listRecords;
    }
}
