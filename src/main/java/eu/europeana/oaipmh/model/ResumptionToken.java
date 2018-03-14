package eu.europeana.oaipmh.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.Date;
import java.util.List;

@XmlType(propOrder = {"completeListSize", "expirationDate", "cursor"})
public class ResumptionToken {

    @XmlAttribute
    private Date expirationDate;

    @XmlAttribute
    private long cursor;

    @XmlAttribute
    private long completeListSize;

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

    public List<String> getFilterQuery() {
        return filterQuery;
    }

    public void setFilterQuery(List<String> filterQuery) {
        this.filterQuery = filterQuery;
    }
}
