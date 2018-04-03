package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import eu.europeana.oaipmh.service.exception.ErrorCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class OAIError {
    @XmlAttribute
    private ErrorCode code;

    @JacksonXmlText
    @XmlValue
    private String value;

    public OAIError() {}

    public OAIError(ErrorCode code, String message) {
        this.code = code;
        this.value = message;
    }

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
