package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.model.response.OAIResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class OAIPMHServiceClient {
    private static final Logger LOG = LogManager.getLogger(OAIPMHServiceClient.class);

    @Value("${oaipmhServer}")
    private String oaipmhServer;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ListIdentifiersQuery listIdentifiersQuery;

    private Map<String, OAIPMHQuery> queries = new HashMap<>();

    public OAIPMHServiceClient() {
    }

    @PostConstruct
    public void initQueries() {
        queries.put("ListIdentifiers", listIdentifiersQuery);
    }

    public String getOaipmhServer() {
        return oaipmhServer;
    }

    public void execute(String verb) {
        OAIPMHQuery verbToExecute = queries.get(verb);
        if (verbToExecute != null) {
            verbToExecute.execute(this);
        }
    }

    public OAIResponse makeRequest(String request, Class<? extends OAIResponse> responseClass) {
        return restTemplate.getForObject(request, responseClass);
    }
}
