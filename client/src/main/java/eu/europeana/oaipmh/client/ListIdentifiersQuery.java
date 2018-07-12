package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.model.Header;
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
public class ListIdentifiersQuery extends BaseQuery implements OAIPMHQuery {

    private static final Logger LOG = LogManager.getLogger(ListIdentifiersQuery.class);

    @Value("${ListIdentifiers.metadataPrefix}")
    private String metadataPrefix;

    @Value("${ListIdentifiers.from}")
    private String from;

    @Value("${ListIdentifiers.until}")
    private String until;

    @Value("${ListIdentifiers.set}")
    private String set;

    @Value("${LogProgress.interval}")
    private Integer logProgressInterval;

    private List<String> sets = new ArrayList<>();

    public ListIdentifiersQuery() {
    }

    public ListIdentifiersQuery(String metadataPrefix, String from, String until, String set, int logProgressInterval) {
        this.metadataPrefix = metadataPrefix;
        this.from = from;
        this.until = until;
        this.set = set;
        this.logProgressInterval = logProgressInterval;
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
            execute(oaipmhServer, null, null);
        } else {
            for (String setName : sets) {
                execute(oaipmhServer, setName, null);
            }
        }
    }

    public List<String> getIdentifiers(OAIPMHServiceClient oaipmhServer) {
        List<String> identifiers = new ArrayList<>();
        if (sets.isEmpty()) {
            execute(oaipmhServer, null, identifiers);
        } else {
            for (String setName : sets) {
                execute(oaipmhServer, setName, identifiers);
            }
        }
        return identifiers;
    }

    private void execute(OAIPMHServiceClient oaipmhServer, String setName, List<String> identifiers) {
        long counter = 0;
        long start = System.currentTimeMillis();
        ProgressLogger logger = new ProgressLogger(-1, logProgressInterval);

        String request = getRequest(oaipmhServer.getOaipmhServer(), setName);

        ListIdentifiersResponse response = (ListIdentifiersResponse) oaipmhServer.makeRequest(request, ListIdentifiersResponse.class);
        ListIdentifiers responseObject = response.getListIdentifiers();
        if (responseObject != null) {
            counter += responseObject.getHeaders().size();
            if (responseObject.getResumptionToken() != null) {
                logger.setTotalItems(responseObject.getResumptionToken().getCompleteListSize());
            } else {
                logger.setTotalItems(responseObject.getHeaders().size());
            }
            collectIdentifiers(responseObject.getHeaders(), identifiers);

            while (responseObject.getResumptionToken() != null) {
                request = getResumptionRequest(oaipmhServer.getOaipmhServer(), responseObject.getResumptionToken().getValue());
                response = (ListIdentifiersResponse) oaipmhServer.makeRequest(request, ListIdentifiersResponse.class);
                responseObject = response.getListIdentifiers();
                if (responseObject == null) {
                    break;
                }
                counter += responseObject.getHeaders().size();
                logger.logProgress(counter);
                collectIdentifiers(responseObject.getHeaders(), identifiers);
            }
        }

        LOG.info("ListIdentifiers for set " + setName + " executed in " + ProgressLogger.getDurationText(System.currentTimeMillis() - start) +
                ". Harvested " + counter + " identifiers.");
    }

    private void collectIdentifiers(List<Header> headers, List<String> identifiers) {
        if (identifiers != null) {
            for (Header header : headers) {
                identifiers.add(header.getIdentifier());
            }
        }
    }

    private String getResumptionRequest(String oaipmhServer, String resumptionToken) {
        return getBaseRequest(oaipmhServer, getVerbName()) +
                String.format(RESUMPTION_TOKEN_PARAMETER, resumptionToken);
    }

    private String getRequest(String oaipmhServer, String set) {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseRequest(oaipmhServer, getVerbName()));
        sb.append(String.format(METADATA_PREFIX_PARAMETER, metadataPrefix));
        if (from != null && !from.isEmpty()) {
            sb.append(String.format(FROM_PARAMETER, from));
        }
        if (until != null && !until.isEmpty()) {
            sb.append(String.format(UNTIL_PARAMETER, until));
        }
        if (set != null) {
            sb.append(String.format(SET_PARAMETER, set));
        }

        return sb.toString();
    }
}
