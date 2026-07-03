package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

import static eu.europeana.oaipmh.model.SerializationConstants.*;

import java.util.Date;

/**
 * This class represents resumption token tag in the ListIdentifiers verb response XML
 */
@XmlRootElement(name=resumptionToken)
@XmlType(propOrder = { completeListSize, expirationDate, cursor })
public class ResumptionToken {

    @XmlAttribute(name=SerializationConstants.expirationDate)
    private Date expirationDate;

    @XmlAttribute(name=SerializationConstants.cursor)
    private long cursor;

    @XmlAttribute(name=SerializationConstants.completeListSize)
    private long completeListSize;

    @JacksonXmlText
    @XmlValue
    private String value;

    public ResumptionToken() {}

    public ResumptionToken(String resumptionToken, long completeListSize
                         , Date expirationDate, long cursor) {
        this.value = resumptionToken;
        this.completeListSize = completeListSize;
        this.expirationDate = expirationDate;
        this.cursor = cursor;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public long getCompleteListSize() {
        return completeListSize;
    }

    public void setCompleteListSize(long completeListSize) {
        this.completeListSize = completeListSize;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String resumptionToken) {
        this.value = resumptionToken;
    }

    public long getCursor() {
        return cursor;
    }

    public void setCursor(long cursor) {
        this.cursor = cursor;
    }
}
