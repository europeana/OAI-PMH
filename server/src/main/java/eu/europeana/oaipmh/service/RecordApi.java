package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Metadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.impl.ListRecordsImpl;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

    /**
     * @see RecordProvider#getRecord(String)
     */
    @Override
    public Record getRecord(String id) throws OaiPmhException {
        ResponseEntity<String> response = getResponseForRecord(id);
        Metadata rdf = new Metadata(response.getBody());

        Header header = new Header(id, new Date(), new ArrayList<>());
        return new Record(header, rdf);
    }

    @Override
    public void checkRecordExists(String id) throws OaiPmhException {
        ResponseEntity<String> response = getResponseForRecord(id);
        if (response == null) {
            throw new IdDoesNotExistException("Record with id '" + id + "' not found");
        }
    }

    @Override
    public ListRecords listRecords(
            List<String> identifiers, ResumptionToken token) 
                throws OaiPmhException {
        List<Record> records = new ArrayList<>();
        for (String id : identifiers) {
            records.add(getRecord(id));
        }
        return new ListRecordsImpl(records, null);
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

        HttpStatusCode responseCode = response.getStatusCode();
        if (HttpStatus.UNAUTHORIZED == responseCode) {
            throw new OaiPmhException("API key is not valid");
        } else if (HttpStatus.NOT_FOUND == responseCode) {
            throw new IdDoesNotExistException("Record with id '"+id+"' not found");
        } else if (HttpStatus.OK != responseCode) {
            throw new OaiPmhException("Error retrieving record. Status = "+response.getStatusCodeValue());
        }
        return response;
    }

    private String constructRequestUrl(String id) {
        StringBuilder url = new StringBuilder(settings.getRecordApiUrl());
        url.append(id);
        url.append(".rdf?");
        url.append(appendWskey());
        return url.toString();
    }

    private String appendWskey() {
        return String.format("wskey=%s", settings.getWskey());
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
