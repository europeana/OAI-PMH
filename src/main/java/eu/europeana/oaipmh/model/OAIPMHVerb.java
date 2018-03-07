package eu.europeana.oaipmh.model;

import eu.europeana.oaipmh.model.response.OAIResponse;

import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({ListIdentifiers.class, GetRecord.class, Identify.class})
public abstract class OAIPMHVerb {

    public abstract OAIResponse getResponse(String baseUrl);
}
