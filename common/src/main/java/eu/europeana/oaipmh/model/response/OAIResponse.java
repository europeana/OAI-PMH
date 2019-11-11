package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.request.OAIRequest;

import javax.xml.bind.annotation.XmlAttribute;
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

    private static final long serialVersionUID = -7158631502815022149L;

    @XmlAttribute
    private String xmlns="http://www.openarchives.org/OAI/2.0/";

    @XmlAttribute(name = "xmlns:xsi")
    private String xmlnsxsi="http://www.w3.org/2001/XMLSchema-instance";

    @XmlAttribute(name = "xsi:schemaLocation")
    private String xsischemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";

    private Date responseDate;

    private OAIRequest request;

    public OAIResponse() {}

    OAIResponse(OAIRequest request) {
        this.responseDate = new Date();
        this.request = request;
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
