package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.model.ListRecords;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.response.ListRecordsResponse;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.ZipOutputStream;

@Component
public class ListRecordsQuery extends BaseQuery implements OAIPMHQuery {

    private static final Logger LOG = LogManager.getLogger(ListRecordsQuery.class);

    @Value("${ListRecords.metadataPrefix}")
    private String metadataPrefix;

    @Value("${ListRecords.from}")
    private String from;

    @Value("${ListRecords.until}")
    private String until;

    @Value("${ListRecords.set}")
    private String set;

    @Value("${ListRecords.useGetRecord}")
    private boolean useGetRecord;

    @Value("${ListRecords.threads}")
    private int threads;

    private ExecutorService threadPool;

    @Value("${LogProgress.interval}")
    private Integer logProgressInterval;

    @Value("${saveToFile}")
    private String saveToFile;

    @Value("${saveToFolder}")
    private String directoryLocation;


    private List<String> sets = new ArrayList<>();

    @PostConstruct
    public void initSets() {
        if (set != null && !set.isEmpty()) {
            sets.addAll(Arrays.asList(set.split(",")));
        }
    }

    private void initThreadPool() {
        // init thread pool
        if (threads < 1) {
            threads = 1;
        }
        threadPool = Executors
                .newFixedThreadPool(threads);
    }

    @Override
    public String getVerbName() {
        return useGetRecord ? Constants.LIST_IDENTIFIERS_VERB : Constants.LIST_RECORDS_VERB;
    }

    @Override
    public void execute(OAIPMHServiceClient oaipmhServer) throws OaiPmhException {
        if (sets.isEmpty()) {
            execute(oaipmhServer, null);
        } else {
            for (String setIdentifier : sets) {
                execute(oaipmhServer, setIdentifier);
            }
        }
    }

    private void execute(OAIPMHServiceClient oaipmhServer, String setIdentifier) {
        if (useGetRecord) {
            executeMultithreadListRecords(oaipmhServer, setIdentifier);
        } else {
            executeListRecords(oaipmhServer, setIdentifier);
        }
    }

    private void executeMultithreadListRecords(OAIPMHServiceClient oaipmhServer, String setIdentifier) {
        initThreadPool();

        long counter = 0;
        long start = System.currentTimeMillis();
        ProgressLogger logger = new ProgressLogger(-1, logProgressInterval);

        ListIdentifiersQuery identifiersQuery = prepareListIdentifiersQuery(setIdentifier);
        List<String> identifiers = identifiersQuery.getIdentifiers(oaipmhServer);
        logger.setTotalItems(identifiers.size());

        List<Future<ListRecordsResult>> results = null;
        List<Callable<ListRecordsResult>> tasks = new ArrayList<>();

        int perThread = identifiers.size() / threads;

        // create task for each resource provider
        for (int i = 0; i < threads; i++) {
            int fromIndex = i * perThread;
            int toIndex = (i + 1) * perThread;
            if (i == threads - 1) {
                toIndex = identifiers.size();
            }
            tasks.add(new ListRecordsExecutor(identifiers.subList(fromIndex, toIndex), metadataPrefix, oaipmhServer, logProgressInterval));
        }

        try {
            // invoke a separate thread for each provider
            results = threadPool.invokeAll(tasks);

            ListRecordsResult listRecordsResult;
            for (Future<ListRecordsResult> result : results) {
                listRecordsResult = result.get();
                LOG.info("Executor finished with {} errors in {} sec.",
                         listRecordsResult.getErrors(), listRecordsResult.getTime());
                counter += perThread;
                logger.logProgress(counter);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted.", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            LOG.error("Problem with task thread execution.", e);
        }

        clean();

        LOG.info("ListRecords for set " + setIdentifier + " executed in " + ProgressLogger.getDurationText(System.currentTimeMillis() - start) +
                ". Harvested " + identifiers.size() + " identifiers.");
    }

    private void executeListRecords(OAIPMHServiceClient oaipmhServer, String setIdentifier) {
        long counter = 0;
        long start = System.currentTimeMillis();
        ProgressLogger logger = new ProgressLogger(-1, logProgressInterval);

        String request = getRequest(oaipmhServer.getOaipmhServer(), setIdentifier);
        ListRecordsResponse response = oaipmhServer.getListRecordRequest(request);
        ListRecords responseObject = response.getListRecords();
        try (final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(new File(directoryLocation + Constants.PATH_SEPERATOR + setIdentifier + Constants.ZIP_EXTENSION)));
             OutputStreamWriter writer = new OutputStreamWriter(zout)) {

            if (StringUtils.equalsIgnoreCase(saveToFile, Constants.TRUE)) {
                for(Record record : responseObject.getRecords()) {
                    ZipUtility.writeInZip(zout, writer, record);
                }
            }
            if (responseObject != null) {
                counter += responseObject.getRecords().size();

                if (responseObject.getResumptionToken() != null) {
                    logger.setTotalItems(responseObject.getResumptionToken().getCompleteListSize());
                } else {
                    logger.setTotalItems(responseObject.getRecords().size());
                }
                while (responseObject.getResumptionToken() != null) {

                    request = getResumptionRequest(oaipmhServer.getOaipmhServer(), responseObject.getResumptionToken().getValue());
                    response =  oaipmhServer.getListRecordRequest(request);
                    responseObject = response.getListRecords();

                    if (StringUtils.equalsIgnoreCase(saveToFile, Constants.TRUE)) {
                        for (Record record : responseObject.getRecords()) {
                            ZipUtility.writeInZip(zout, writer, record);
                        }
                    }
                    if (responseObject == null) {

                        break;
                    }
                    counter += responseObject.getRecords().size();
                    logger.logProgress(counter);
                }
            }
        } catch (IOException e) {
            LOG.error("Error creating outputStreams ", e);
        }


        LOG.info("ListRecords for set " + setIdentifier + " executed in " + ProgressLogger.getDurationText(System.currentTimeMillis() - start) +
                ". Harvested " + counter + " records.");
    }

    private ListIdentifiersQuery prepareListIdentifiersQuery(String setIdentifier) {
        ListIdentifiersQuery query = new ListIdentifiersQuery(metadataPrefix, from, until, setIdentifier, 30);
        query.initSets();
        return query;
    }

    private String getResumptionRequest(String oaipmhServer, String resumptionToken) {
        return getBaseRequest(oaipmhServer, getVerbName()) +
                String.format(RESUMPTION_TOKEN_PARAMETER, resumptionToken);
    }

    private String getRequest(String oaipmhServer, String setIdentifier) {
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
            sb.append(String.format(SET_PARAMETER, setIdentifier));
        }

        return sb.toString();
    }

    @PreDestroy
    private void clean() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }
}
