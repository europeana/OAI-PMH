package eu.europeana.oaipmh.model;

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
    private Object responseObject;

    public OAIResponse(String verb, String baseUrl, Object object) {
        this.responseDate = new Date();
        this.request = new OAIRequest(verb, baseUrl);
        this.responseObject = object;
    }
}
