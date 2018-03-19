package eu.europeana.oaipmh.model.request;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Basic OAI request data (always part of OAI response)
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
public class OAIRequest {

    @XmlAttribute
    private String verb;

    @JacksonXmlText
    private String baseUrl;

    public OAIRequest() {}

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
