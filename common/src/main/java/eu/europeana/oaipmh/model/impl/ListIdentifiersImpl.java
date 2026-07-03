package eu.europeana.oaipmh.model.impl;

import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.SerializationConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class represents the ListIdentifiers tag in the ListIdentifiers verb XML response
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListIdentifiersImpl implements ListIdentifiers {

    @XmlElement(name=SerializationConstants.header)
    private List<Header> headers;

    @XmlElement(name=SerializationConstants.resumptionToken)
    private ResumptionToken resumptionToken;

    public ListIdentifiersImpl() {
        this.headers = new ArrayList<>();
    }

    public ListIdentifiersImpl(List<Header> headers) {
        this(headers, null);
    }

    public ListIdentifiersImpl(List<Header> headers
                             , ResumptionToken resumptionToken) {
        this.headers = headers;
        this.resumptionToken = resumptionToken;
    }

    @Override
    public boolean isEmpty() {
        return headers.isEmpty();
    }

    @Override
    public Stream<Header> stream() {
        return headers.stream();
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    @Override
    public ResumptionToken getResumptionToken() { return resumptionToken; }

    public void setResumptionToken(ResumptionToken resumptionToken) { this.resumptionToken = resumptionToken; }

    @Override
    public void close() throws Exception {
    }

    public static class Adapter extends XmlAdapter<ListIdentifiersImpl, ListIdentifiers> {

        public ListIdentifiers unmarshal(ListIdentifiersImpl v) { 
            return v; 
        }

        public ListIdentifiersImpl marshal(ListIdentifiers v) { 
            return (ListIdentifiersImpl)v;
        }
    }
}