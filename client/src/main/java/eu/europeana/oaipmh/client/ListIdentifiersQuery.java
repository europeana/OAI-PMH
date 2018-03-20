package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.model.ListIdentifiers;
import eu.europeana.oaipmh.model.response.ListIdentifiersResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ListIdentifiersQuery implements OAIPMHQuery {

    private static final Logger LOG = LogManager.getLogger(ListIdentifiersQuery.class);

    private static final String METADATA_PREFIX = "&metadataPrefix=%s";

    private static final String FROM = "&from=%s";

    private static final String UNTIL = "&until=%s";

    private static final String SET = "&set=%s";

    private static final String RESUMPTION_TOKEN = "&resumptionToken=%s";

    @Value("${ListIdentifiers.metadataPrefix}")
    private String metadataPrefix;

    @Value("${ListIdentifiers.from}")
    private String from;

    @Value("${ListIdentifiers.until}")
    private String until;

    @Value("${ListIdentifiers.set}")
    private String set;

    private List<String> sets = new ArrayList<>();

    public ListIdentifiersQuery() {
    }

    @PostConstruct
    public void initSets() {
        if (set != null && !set.isEmpty()) {
            sets.addAll(Arrays.asList(set.split(",")));
        }
    }

    @Override
    public String getVerbName() {
        return "ListIdentifiers";
    }

    @Override
    public void execute(OAIPMHServiceClient oaipmhServer) {
        if (sets.isEmpty()) {
            execute(oaipmhServer, null);
        } else {
            for (String set : sets) {
                execute(oaipmhServer, set);
            }
        }
    }

    private void execute(OAIPMHServiceClient oaipmhServer, String set) {
        long counter = 0;
        long start = System.currentTimeMillis();

        String request = getRequest(oaipmhServer.getOaipmhServer(), set);
        ListIdentifiersResponse response = (ListIdentifiersResponse) oaipmhServer.makeRequest(request, ListIdentifiersResponse.class);
        ListIdentifiers responseObject = response.getListIdentifiers();
        if (responseObject != null) {
            counter += responseObject.getHeaders().size();

            while (responseObject.getResumptionToken() != null) {
                request = getResumptionRequest(oaipmhServer.getOaipmhServer(), responseObject.getResumptionToken().getValue());
                response = (ListIdentifiersResponse) oaipmhServer.makeRequest(request, ListIdentifiersResponse.class);
                responseObject = response.getListIdentifiers();
                if (responseObject == null) {
                    break;
                }
                counter += responseObject.getHeaders().size();
            }
        }

        LOG.info("ListIdentifiers for set " + set + " executed in " + String.valueOf(System.currentTimeMillis() - start) + " ms. Harvested " + counter + " identifiers.");
    }

    private String getResumptionRequest(String oaipmhServer, String resumptionToken) {
        return getBaseRequest(oaipmhServer) +
                String.format(RESUMPTION_TOKEN, resumptionToken);
    }

    private String getBaseRequest(String oaipmhServer) {
        return oaipmhServer +
                "?" +
                String.format(VERB_PARAMETER, getVerbName());
    }

    private String getRequest(String oaipmhServer, String set) {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseRequest(oaipmhServer));
        sb.append(String.format(METADATA_PREFIX, metadataPrefix));
        if (from != null && !from.isEmpty()) {
            sb.append(String.format(FROM, from));
        }
        if (until != null && !until.isEmpty()) {
            sb.append(String.format(UNTIL, until));
        }
        if (set != null) {
            sb.append(String.format(SET, set));
        }

        return sb.toString();
    }
}
