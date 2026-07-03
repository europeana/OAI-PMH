package eu.europeana.oaipmh.model.response;

import eu.europeana.oaipmh.model.OAIPMHVerb;
import eu.europeana.oaipmh.model.request.OAIRequest;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.europeana.oaipmh.model.SerializationConstants;

import java.util.Date;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

/**
 * Basic OAI response
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@XmlRootElement(name = OAIPMH)
@JsonIgnoreProperties({ XMLNS, XSI_LOCATION, schemaLocation })
@XmlType(propOrder={ responseDate, request
                   , error, GetRecord, ListRecords, Identify, ListIdentifiers
                   , ListMetadataFormats, ListSets })
public class OAIResponse implements AutoCloseable {

    @XmlElement(name=SerializationConstants.responseDate)
    private Date responseDate;

    @XmlElement(name=SerializationConstants.request)
    private OAIRequest request;

    @JsonAlias({ error, GetRecord, ListRecords, Identify, ListIdentifiers, ListMetadataFormats, ListSets })
    private OAIPMHVerb verb;

    protected OAIResponse() {}

    public OAIResponse(OAIRequest request, OAIPMHVerb verb) {
        this.responseDate = new Date();
        this.request      = request;
        this.verb         = verb;
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

    public OAIPMHVerb getVerb() {
        return verb;
    }

    public void setVerb(OAIPMHVerb verb) {
        this.verb = verb;
    }

    @Override
    public void close() throws Exception {
    }
}
