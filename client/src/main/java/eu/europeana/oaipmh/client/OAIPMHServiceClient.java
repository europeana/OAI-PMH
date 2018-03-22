package eu.europeana.oaipmh.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.europeana.oaipmh.model.response.ListIdentifiersResponse;
import eu.europeana.oaipmh.model.response.OAIResponse;
import eu.europeana.oaipmh.model.serialize.ListIdentifiersResponseDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OAIPMHServiceClient {
    private static final Logger LOG = LogManager.getLogger(OAIPMHServiceClient.class);

    @Value("${oaipmhServer}")
    private String oaipmhServer;

    private RestTemplate restTemplate = new RestTemplate();

    private ObjectMapper mapper;

    @Autowired
    private ListIdentifiersQuery listIdentifiersQuery;

    private Map<String, OAIPMHQuery> queries = new HashMap<>();

    public OAIPMHServiceClient() {
    }

    @PostConstruct
    public void init() {
        queries.put("ListIdentifiers", listIdentifiersQuery);
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ListIdentifiersResponse.class, new ListIdentifiersResponseDeserializer());
        mapper.registerModule(module);
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
        OAIResponse response = null;
        String responseAsString = restTemplate.getForObject(request, String.class);
        String json = XML.toJSONObject(responseAsString).toString();
        try {
            response = mapper.readValue(json, responseClass);
        } catch (IOException e) {
            LOG.error("Exception when deserializing response.", e);
        }

        return response;
    }
}
