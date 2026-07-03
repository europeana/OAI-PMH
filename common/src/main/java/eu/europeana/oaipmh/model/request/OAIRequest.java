package eu.europeana.oaipmh.model.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import eu.europeana.oaipmh.model.SerializationConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Basic OAI request data (always part of OAI response)
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */ 
@XmlSeeAlso({ ListIdentifiersRequest.class, GetRecordRequest.class
            , IdentifyRequest.class, ListMetadataFormatsRequest.class
            , ListSetsRequest.class })
@XmlJavaTypeAdapter(OAIRequestAdapter.class)
@XmlAccessorType(XmlAccessType.FIELD)
public class OAIRequest {

    @XmlAttribute(name=SerializationConstants.verb)
    private String verb;

    @JacksonXmlText
    private String baseUrl;

    protected OAIRequest() {}

    public OAIRequest(String verb, String baseUrl) {
        this.verb = verb;
        this.baseUrl = baseUrl;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
