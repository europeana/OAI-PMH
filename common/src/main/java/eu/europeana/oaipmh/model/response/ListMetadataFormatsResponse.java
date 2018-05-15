package eu.europeana.oaipmh.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlElement;

/**
 * The class represents the response for the ListMetadataFormats verb request
 */
public class ListMetadataFormatsResponse extends OAIResponse {

    private ListMetadataFormats listMetadataFormats;

    public ListMetadataFormatsResponse() {}

    public ListMetadataFormatsResponse(ListMetadataFormats listMetadataFormats, OAIRequest request) {
        super(request);
        this.listMetadataFormats = listMetadataFormats;
    }

    public void setListMetadataFormats(ListMetadataFormats listMetadataFormats) {
        this.listMetadataFormats = listMetadataFormats;
    }

    @XmlElement(name="ListMetadataFormats")
    @JacksonXmlProperty(localName = "ListMetadataFormats")
    public ListMetadataFormats getListMetadataFormats() {
        return listMetadataFormats;
    }
}
