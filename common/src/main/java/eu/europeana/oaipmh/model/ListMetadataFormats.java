package eu.europeana.oaipmh.model;

import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.ListMetadataFormatsResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the ListIdentifiers tag in the ListIdentifiers verb XML response
 */
@XmlRootElement(name="ListMetadataFormats")
public class ListMetadataFormats extends OAIPMHVerb {

    private static final long serialVersionUID = -8111855326108565425L;

    @XmlElement(name="metadataFormat")
    private List<MetadataFormat> metadataFormats;

    public ListMetadataFormats() {
        this.metadataFormats = new ArrayList<>();
    }

    public ListMetadataFormats(List<MetadataFormat> metadataFormats) {
        this.metadataFormats = metadataFormats;
    }

    public List<MetadataFormat> getMetadataFormats() {
        return metadataFormats;
    }

    public void setMetadataFormats(List<MetadataFormat> metadataFormats) {
        this.metadataFormats = metadataFormats;
    }

    @Override
    public OAIResponse getResponse(OAIRequest request) {
        return new ListMetadataFormatsResponse(this, request);
    }
}
