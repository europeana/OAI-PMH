package com.ontotext.walk;

import com.ontotext.helper.WatchDog;
import com.ontotext.process.ListProcessor;
import com.ontotext.process.RecordProcessor;
import com.ontotext.query.QueryListRecords;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;

/**
 * Created by Simo on 14-1-30.
 */
public class ListRecordsWalker implements Runnable {
    private final OaiPmhServer server;
    public final RecordProcessor recordProcessor;
    public final ListProcessor listProcessor;
    private final QueryListRecords query;
    private final Navigator<RecordsList> navigator;
    Log log = LogFactory.getLog(ListRecordsWalker.class);
    WatchDog watchDog = new WatchDog(10);

    public ListRecordsWalker(OaiPmhServer server,
                             RecordProcessor recordProcessor,
                             ListProcessor listProcessor,
                             QueryListRecords query,
                             Navigator<RecordsList> navigator) {
        this.server = server;
        this.recordProcessor = recordProcessor;
        this.listProcessor = listProcessor;
        this.query = query;
        this.navigator = navigator;
    }

    public void runThrow() throws OAIException {
        RecordsList recordsList = listRecords(query);

        do {
            navigator.check(recordsList);
            if (navigator.shouldStop()) {
                break;
            }
            listProcessor.processListBegin(recordsList);
            for (Record record : recordsList.asList()) {
                recordProcessor.processRecord(record);
            }
            listProcessor.processListEnd(recordsList);
            ResumptionToken resumptionToken = recordsList.getResumptionToken();
            if (resumptionToken == null) {
                break;
            }
            recordsList = listRecords(resumptionToken);
        } while (recordsList.size() > 0);
    }

    private RecordsList listRecords(ResumptionToken resumptionToken) throws OAIException {
        RecordsList recordsList = server.listRecords(resumptionToken);
        watchDog.reset();
        return recordsList;
    }

    private RecordsList listRecords(QueryListRecords query) throws OAIException {
        RecordsList recordsList = server.listRecords(query.prefix, query.from, query.until, query.set);
        watchDog.reset();
        return recordsList;
    }

    public void run() {
        try {
            runThrow();
        } catch (OAIException e) {
            log.error("Exiting ...", e);
            listProcessor.processListError(e);
        }
    }
}
