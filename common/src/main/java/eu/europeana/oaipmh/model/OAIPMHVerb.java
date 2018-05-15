package eu.europeana.oaipmh.model;

import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.OAIResponse;

import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;

/**
 * Abstract class for a tag representing response for a verb
 */
@XmlSeeAlso({ListIdentifiers.class, GetRecord.class, Identify.class, ListMetadataFormats.class, ListSets.class})
public abstract class OAIPMHVerb implements Serializable {

    public abstract OAIResponse getResponse(OAIRequest request);
}
