package eu.europeana.oaipmh.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.Date;
import java.util.List;

/**
 * This class represents resumption token tag in the ListIdentifiers verb response XML
 */
@XmlType(propOrder = {"completeListSize", "expirationDate", "cursor"})
public class ResumptionToken {

    @XmlAttribute
    private Date expirationDate;

    @XmlAttribute
    private long cursor;

    @XmlAttribute
    private long completeListSize;

    @JacksonXmlText
    @XmlValue
    private String value;

    @XmlTransient
    private List<String> filterQuery;

    public ResumptionToken() {}

    public ResumptionToken(String resumptionToken, long completeListSize, Date expirationDate, long cursor, List<String> filterQuery) {
        this.value = resumptionToken;
        this.completeListSize = completeListSize;
        this.expirationDate = expirationDate;
        this.cursor = cursor;
        this.filterQuery = filterQuery;
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

    @XmlTransient
    public List<String> getFilterQuery() {
        return filterQuery;
    }

    @XmlTransient
    public void setFilterQuery(List<String> filterQuery) {
        this.filterQuery = filterQuery;
    }
}
