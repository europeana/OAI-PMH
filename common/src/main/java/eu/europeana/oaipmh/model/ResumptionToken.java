package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;
import java.util.Date;

/**
 * This class represents resumption token tag in the ListIdentifiers verb response XML
 */
@XmlType(propOrder = {"completeListSize", "expirationDate", "cursor"})
public class ResumptionToken implements Serializable {

    @XmlAttribute
    private Date expirationDate;

    @XmlAttribute
    private long cursor;

    @XmlAttribute
    private long completeListSize;

    @JacksonXmlText
    @XmlValue
    private String value;

    public ResumptionToken() {}

    public ResumptionToken(String resumptionToken, long completeListSize, Date expirationDate, long cursor) {
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
