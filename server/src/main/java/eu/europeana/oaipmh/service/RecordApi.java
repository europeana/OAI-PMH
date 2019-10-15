package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.RDFMetadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Retrieve record xml information from the Europeana Record API
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
public class RecordApi extends BaseProvider implements RecordProvider {

    private static final Logger LOG = LogManager.getLogger(RecordApi.class);

    @Value("${recordApiUrl}")
    private String recordApiUrl;

    @Value("${wskey}")
    private String wskey;

    /**
     * @see RecordProvider#getRecord(String)
     */
    @Override
    public Record getRecord(String id) throws OaiPmhException {
        ResponseEntity<String> response = getResponseForRecord(id);
        RDFMetadata rdf = new RDFMetadata(response.getBody());

        Header header = new Header();
        header.setIdentifier(id);
        header.setDatestamp(new Date());
        return new Record(header, rdf);
    }

    private ResponseEntity<String> getResponseForRecord(String id) throws OaiPmhException {
        if (id == null) {
            throw new IdDoesNotExistException(id);
        }

        String recordId = prepareRecordId(id);

        // construct url
        String requestUrl = constructRequestUrl(recordId.substring(1));

        LOG.debug("Request is {}", requestUrl);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ApiResponseErrorHandler());
        ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
        LOG.debug("Response = {}", response);

        HttpStatus responseCode = response.getStatusCode();
        if (HttpStatus.UNAUTHORIZED == responseCode) {
            throw new OaiPmhException("API key is not valid");
        } else if (HttpStatus.NOT_FOUND == responseCode) {
            throw new IdDoesNotExistException("Record with id '"+id+"' not found");
        } else if (HttpStatus.OK != responseCode) {
            throw new OaiPmhException("Error retrieving record. Status = "+response.getStatusCodeValue());
        }
        return response;
    }

    @Override
    public void checkRecordExists(String id) throws OaiPmhException {
        ResponseEntity<String> response = getResponseForRecord(id);
        if (response == null) {
            throw new IdDoesNotExistException("Record with id '" + id + "' not found");
        }
    }

    @Override
    public ListRecords listRecords(List<Header> identifiers) throws OaiPmhException {
        ListRecords listRecords = new ListRecords();
        List<Record> records = new ArrayList<>();
        for (Header header : identifiers) {
            records.add(getRecord(header.getIdentifier()));
        }
        listRecords.setRecords(records);
        return listRecords;
    }

    private String constructRequestUrl(String id) {
        StringBuilder url = new StringBuilder(recordApiUrl);
        url.append(id);
        url.append(".rdf?");
        url.append(appendWskey());
        return url.toString();
    }

    private String appendWskey() {
        return String.format("wskey=%s", wskey);
    }

    /**
     * @see RecordProvider#close()
     */
    public void close() {
        // not needed in this case
    }

    /**
     * Empty error handling to avoid the rest template throwing errors (we want to throw our own exceptions)
     */
    private static class ApiResponseErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return false;
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // errors are handled elsewhere
        }
    }
}
