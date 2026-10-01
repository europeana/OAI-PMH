package eu.europeana.oaipmh.service;

import eu.europeana.api.commons_sb3.auth.AuthenticationHandler;
import eu.europeana.api.commons_sb3.http.HttpConnection;
import eu.europeana.oaipmh.model.Header;
import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Metadata;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.ResumptionToken;
import eu.europeana.oaipmh.model.impl.ListRecordsImpl;
import eu.europeana.oaipmh.service.exception.IdDoesNotExistException;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Retrieve record xml information from the Europeana Record API
 * @author Patrick Ehlert
 * Created on 28-02-2018
 */
public class RecordApi extends BaseProvider implements RecordProvider {

    private static final Logger LOG = LogManager.getLogger(RecordApi.class);

    private final HttpConnection connection;
    protected AuthenticationHandler auth;


    public RecordApi(AuthenticationHandler auth) {
        this.connection = new HttpConnection(true);
        this.auth = auth;

    }

    /**
     * Retrieves a record based on its unique identifier.
     * The method interacts with the record API to fetch the corresponding response
     * data and constructs a {@link java.lang.Record} object. If the response is null, a
     * {@link java.lang.Record} containing header information is returned.
     *
     * @param id the unique identifier of the record to retrieve. Must not be null.
     * @return the {@link java.lang.Record} object containing the metadata and header information,
     *         or null if the record is not found.
     * @throws OaiPmhException if an error occurs during the retrieval process,
     *         such as API communication errors or unexpected response status.
     */
    @Override
    public Record getRecord(String id) throws OaiPmhException {
        String response = getResponseForRecord(id);
        if (response != null) {
            Metadata rdf = new Metadata(response);
            Header header = new Header(id, new Date(), new ArrayList<>());
            return new Record(header, rdf);
        }
        return null;
    }

    /**
     * Validates the existence of a record based on its unique identifier.
     * The method checks if a response for the given record identifier exists.
     * If no record is found, an exception is thrown.
     *
     * @param id the unique identifier of the record to be validated. Must not be null.
     * @throws OaiPmhException if an error occurs during the validation process.
     * @throws IdDoesNotExistException if no record with the specified identifier exists.
     */
    @Override
    public void checkRecordExists(String id) throws OaiPmhException {
        if (getResponseForRecord(id) == null) {
            throw new IdDoesNotExistException("Record with id '" + id + "' not found");
        }
    }

    /**
     * Retrieves a list of records based on their unique identifiers and an optional resumption token.
     * This method interacts with the record API to fetch the corresponding records and returns them
     * in a {@code ListRecords} object.
     *
     * @param identifiers a {@code List<String>} containing the unique identifiers of the records to retrieve. Must not be null.
     * @param token a {@link ResumptionToken} object representing the pagination token for continued retrieval, or null if not applicable.
     * @return a {@link ListRecords} object containing the retrieved records and pagination details, if any.
     * @throws OaiPmhException if an error occurs during the retrieval process, such as API communication errors or unexpected response status.
     */
    @Override
    public ListRecords listRecords(List<String> identifiers, ResumptionToken token) throws OaiPmhException {
        List<Record> records = new ArrayList<>();
        for (String id : identifiers) {
            records.add(getRecord(id));
        }
        return new ListRecordsImpl(records, null);
    }

    /**
     * Retrieves the response for a record based on its unique identifier.
     * This method connects to the record API, sends a request, and retrieves the response.
     *
     * @param id the unique identifier of the record. Must not be null.
     * @return the response in JSON format as a String if the request is successful.
     * @throws IdDoesNotExistException if the identifier is null or if no record with the specified identifier exists.
     * @throws OaiPmhException if an error occurs during communication with the API or if the response status is unexpected.
     */
    private String getResponseForRecord(String id) throws OaiPmhException {
        if (id == null) {
            throw new IdDoesNotExistException(id);
        }
        try (CloseableHttpResponse httpResponse = connection.get(buildRecordApiUrl(id).toString(),
                Collections.singletonMap(HttpHeaders.ACCEPT,"application/json") ,auth)) {
            if (httpResponse.getCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(httpResponse.getEntity());
            }
            if (httpResponse.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                throw new OaiPmhException("API key is not valid");
            }
            else if (httpResponse.getCode() == HttpStatus.SC_NOT_FOUND) {
                throw new IdDoesNotExistException("Record with id '"+id+"' not found");
            }
            else {
                LOG.error("Unable to get the valid response from the Search and Record API");
                throw new OaiPmhException("Error retrieving record. Status = " + httpResponse.getCode());
            }
        } catch (IOException | ParseException e) {
           throw new OaiPmhException("Error retrieving record = " +e.getMessage(), e);
        }
    }


    /**
     * Constructs a record API URL using the provided record identifier.
     *
     * @param id the unique identifier of the record, which will be appended
     *           to the base API URL with the ".rdf" suffix
     * @return a constructed {@code URI} representing the complete record API URL
     * @throws OaiPmhException if a {@code URISyntaxException} occurs while building the URL
     */
    public URI buildRecordApiUrl(String id) throws OaiPmhException {
        try {
            return new URIBuilder(settings.getRecordApiUrl()).appendPath(id + ".rdf").build();
        } catch (URISyntaxException e) {
            throw  new OaiPmhException("Error creating recprd api Urls " +e.getMessage() + HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (IOException e) {
            throw new OaiPmhException("Error closing the httpconnection for RecordAPI ", e);
        }
    }
}
