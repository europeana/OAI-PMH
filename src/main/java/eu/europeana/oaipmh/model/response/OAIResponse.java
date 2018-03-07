package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.OAIPMHVerb;
import eu.europeana.oaipmh.model.OAIRequest;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * Basic OAI response
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@XmlRootElement(name = "OAI-PMH")
public class OAIResponse implements Serializable {

    private Date responseDate;

    private OAIRequest request;

    private OAIPMHVerb responseObject;

    OAIResponse(String baseUrl, OAIPMHVerb object) {
        this.responseDate = new Date();
        this.request = new OAIRequest(object.getClass().getSimpleName(), baseUrl);
        this.responseObject = object;
    }

    public OAIPMHVerb getResponseObject() {
        return responseObject;
    }
}
