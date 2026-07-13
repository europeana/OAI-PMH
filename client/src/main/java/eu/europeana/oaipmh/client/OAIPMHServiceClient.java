package eu.europeana.oaipmh.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.oaipmh.model.response.OAIResponse;
import eu.europeana.oaipmh.model.serialize.DefaultSerializationProvider;
import eu.europeana.oaipmh.model.serialize.SerializationHandler;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
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

    @Autowired
    private GetRecordQuery getRecordQuery;

    @Autowired
    private ListRecordsQuery listRecordsQuery;

    private Map<String, OAIPMHQuery> queries = new HashMap<>();

    @PostConstruct
    public void init() {
        SerializationHandler.register(new DefaultSerializationProvider());
        mapper = SerializationHandler.getSerialization();
        queries.put("ListIdentifiers", listIdentifiersQuery);
        queries.put("GetRecord", getRecordQuery);
        queries.put("ListRecords", listRecordsQuery);
    }

    public String getOaipmhServer() {
        return oaipmhServer;
    }

    public void execute(String verb) throws OaiPmhException {
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
