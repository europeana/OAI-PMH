package eu.europeana.oaipmh.model;

import java.io.Serializable;

/**
 * Container for record xml information
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
public class GetRecord implements Serializable {

    private static final long serialVersionUID = -8111845326100870425L;

    private String xmlData;

    public GetRecord(String xmlData) {
        this.xmlData = xmlData;
    }
}
