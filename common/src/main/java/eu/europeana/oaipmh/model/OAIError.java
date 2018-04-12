package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import eu.europeana.oaipmh.model.request.OAIRequest;
import eu.europeana.oaipmh.model.response.ErrorResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;
import eu.europeana.oaipmh.service.exception.ErrorCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class OAIError extends OAIPMHVerb {
    @XmlAttribute
    private String code;

    @JacksonXmlText
    @XmlValue
    private String value;

    public OAIError() {}

    public OAIError(ErrorCode code, String message) {
        this.code = code.toString();
        this.value = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code.toString();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public OAIResponse getResponse(OAIRequest request) {
        return new ErrorResponse(this, request);
    }
}
