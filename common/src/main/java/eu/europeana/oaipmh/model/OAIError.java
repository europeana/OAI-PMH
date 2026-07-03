package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import eu.europeana.oaipmh.service.exception.ErrorCode;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

@XmlRootElement(name = error)
public class OAIError implements OAIPMHVerb {

    @XmlAttribute(name=SerializationConstants.code)
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

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
