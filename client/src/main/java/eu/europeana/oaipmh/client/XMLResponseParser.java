package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.model.*;
import eu.europeana.oaipmh.model.response.GetRecordResponse;
import eu.europeana.oaipmh.model.response.ListRecordsResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class XMLResponseParser {

    private static final Logger LOG = LogManager.getLogger(XMLResponseParser.class);


    public static GetRecordResponse parseGetRecordResponse(String responseAsString) {
        GetRecordResponse recordResponse = new GetRecordResponse();
        InputStream targetStream = new ByteArrayInputStream(responseAsString.getBytes());
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLEventReader eventReader = factory.createXMLEventReader(targetStream);

            GetRecord getRecord = new GetRecord();
            Record record = new Record();

            while (eventReader.hasNext()) {
                XMLEvent xmlEvent = eventReader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    switch (startElement.getName().getLocalPart()) {
                        case Constants.HEADER_TAG:
                            parseHeaderResource(eventReader, record);
                            break;
                        case Constants.MEATADATA_TAG:
                            paraseMetadataString(responseAsString, record.getHeader().getIdentifier(), record);
                            break;
                    }
                }
            }
            getRecord.setRecord(record);
            recordResponse.setGetRecord(getRecord);
        } catch (XMLStreamException e) {
            LOG.debug("Error parsing GetRecordResponse {} ", e);
        } catch (ParseException e) {
            LOG.debug("Error parsing Datestamp {} ", e);
        }
        return recordResponse;
    }


    public static ListRecordsResponse parseListRecordResponse(String responseAsString) {
        ListRecordsResponse recordResponse = new ListRecordsResponse();
        InputStream targetStream = new ByteArrayInputStream(responseAsString.getBytes());

        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLEventReader eventReader = factory.createXMLEventReader(targetStream);

            ListRecords listRecords = new ListRecords();
            List<Record> recordList = new ArrayList<>();
            Record record = null;
            ResumptionToken resumptionToken = null;

            while (eventReader.hasNext()) {
                XMLEvent xmlEvent = eventReader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    //As soo as record tag is opened, create new record object
                    if (Constants.RECORD_TAG.equalsIgnoreCase(startElement.getName().getLocalPart())) {
                        record = new Record();
                    }
                    //get the header and metadata tag values
                    switch (startElement.getName().getLocalPart()) {
                        case Constants.HEADER_TAG:
                            parseHeaderResource(eventReader, record);
                            break;
                        case Constants.MEATADATA_TAG:
                            paraseMetadataString(responseAsString, record.getHeader().getIdentifier(), record);
                            break;
                    }
                    //get the resumption token value
                    if (Constants.RESUMPTIONTOKEN_TAG.equalsIgnoreCase(startElement.getName().getLocalPart())) {
                        resumptionToken = new ResumptionToken();
                        parseResumptionToken(startElement, resumptionToken);
                        resumptionToken.setValue(eventReader.getElementText());
                    }
                }
                if (xmlEvent.isEndElement() && StringUtils.equalsIgnoreCase(xmlEvent.asEndElement().getName().getLocalPart(), Constants.RECORD_TAG)) {
                    recordList.add(record);
                }
            }

            listRecords.setRecords(recordList);
            listRecords.setResumptionToken(resumptionToken);
            recordResponse.setListRecords(listRecords);

        } catch (XMLStreamException e) {
            LOG.debug("Error parsing ListRecordResponse {} ", e);
        } catch (ParseException e) {
            LOG.debug("Error parsing Datestamp {} ", e);
        }
        return recordResponse;
    }

    private static void parseHeaderResource(XMLEventReader xmlEventReader, Record record) throws XMLStreamException, ParseException {
        Header header = new Header();
        record.setHeader(header);

        //get identifier, date, spetspec
        while (xmlEventReader.hasNext()) {
            XMLEvent e = xmlEventReader.nextEvent();
            if (e.isStartElement()) {
                StartElement se = e.asStartElement();

                switch (se.getName().getLocalPart()) {
                    case Constants.IDENTIFIER_TAG:
                        header.setIdentifier(xmlEventReader.getElementText());
                        break;
                    case Constants.DATESTAMP_TAG:
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
                        Date parsedDate = simpleDateFormat.parse(xmlEventReader.getElementText());
                        header.setDatestamp(parsedDate);
                        break;
                    case Constants.SETSPEC_TAG:
                        header.setSetSpec(xmlEventReader.getElementText());
                        break;
                }
            }
            if (e.isEndElement() && StringUtils.equalsIgnoreCase(e.asEndElement().getName().getLocalPart(), Constants.HEADER_TAG)) {
                break;
            }
        }
    }

    private static void paraseMetadataString(String response, String identifier, Record record) {
        RDFMetadata metadata = new RDFMetadata();
        record.setMetadata(metadata);
        String metadataValue = null;

        if (StringUtils.contains(response, identifier)) {
            int metadatastartIndex = StringUtils.indexOf(response, "<metadata>", StringUtils.indexOf(response, identifier));
            int metadatEndIndex = StringUtils.indexOf(response, "</metadata>", metadatastartIndex);
            metadataValue = StringUtils.substring(response, metadatastartIndex + 10, metadatEndIndex);
        }
        metadata.setMetadata(metadataValue);
    }

    private static void parseResumptionToken(StartElement startElement, ResumptionToken resumptionToken) throws ParseException {
        @SuppressWarnings("unchecked")
        Iterator<Attribute> iterator = startElement.getAttributes();

        while (iterator.hasNext()) {
            Attribute attribute = iterator.next();
            QName name = attribute.getName();
            if (Constants.COMPLETELISTSIZE_TAG.equalsIgnoreCase(name.getLocalPart())) {
                resumptionToken.setCompleteListSize(Integer.valueOf(attribute.getValue()));
            }
            if (Constants.EXPIRATIONDATE_TAG.equalsIgnoreCase(name.getLocalPart())) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
                Date parsedDate = simpleDateFormat.parse(attribute.getValue());
                resumptionToken.setExpirationDate(parsedDate);
            }
            if (Constants.CURSOR_TAG.equalsIgnoreCase(name.getLocalPart())) {
                resumptionToken.setCursor(Integer.valueOf(attribute.getValue()));
            }

        }
    }
}
