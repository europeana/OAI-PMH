package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.Header;
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
 * Retrieve information from Search API
 */
public class SearchApi extends CommonApi implements IdentifierProvider {

    private static final Logger LOG = LogManager.getLogger(SearchApi.class);

    @Value("${searchApiUrl}")
    private String searchApiUrl;

    @Override
    public List<Header> listIdentifiers(String metadataPrefix, Date from, Date until, String set) throws OaiPmhException {

        StringBuilder sb = new StringBuilder(searchApiUrl);
        sb.append("?").append(appendWskey());
        sb.append("&query=*");

        List<String> sets = new ArrayList<>();
        sets.add("set1");
        sets.add("set2");
        Header header = new Header("id", new Date(), sets);
        List<Header> result = new ArrayList<>();
        result.add(header);
        Header header2 = new Header("id2", new Date(), sets);
        result.add(header2);
        return result;
    }

}
