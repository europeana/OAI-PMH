package eu.europeana.oaipmh.model;

import eu.europeana.oaipmh.model.response.GetRecordResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;

import java.io.Serializable;

/**
 * Container for record xml information
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
public class GetRecord extends OAIPMHVerb implements Serializable {

    private static final long serialVersionUID = -8111845326100870425L;

    private String xmlData;

    public GetRecord() {
        this.xmlData = "";
    }

    public GetRecord(String xmlData) {
        this.xmlData = xmlData;
    }

    @Override
    public OAIResponse getResponse(String baseUrl) {
        return new GetRecordResponse(baseUrl, this);
    }
}
