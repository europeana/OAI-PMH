package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.OAIPMHVerb;
import eu.europeana.oaipmh.model.request.OAIRequest;
import org.springframework.web.bind.annotation.ResponseBody;

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

    public OAIResponse() {}

    OAIResponse(OAIPMHVerb object, OAIRequest request) {
        this.responseDate = new Date();
        this.request = request;
        this.responseObject = object;
    }

    public void setResponseObject(OAIPMHVerb object) {
        this.responseObject = object;
    }

    public OAIPMHVerb getResponseObject() {
        return responseObject;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
    }

    public OAIRequest getRequest() {
        return request;
    }

    public void setRequest(OAIRequest request) {
        this.request = request;
    }
}
