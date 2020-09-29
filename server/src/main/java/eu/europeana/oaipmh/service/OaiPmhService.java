package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.model.*;
import eu.europeana.oaipmh.model.metadata.MetadataFormatsProvider;
import eu.europeana.oaipmh.model.request.*;
import eu.europeana.oaipmh.service.exception.*;
import eu.europeana.oaipmh.util.DateConverter;
import eu.europeana.oaipmh.util.ResumptionTokenHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

/**
 *
 *
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@Service
public class OaiPmhService extends BaseService {

    private static final Logger LOG = LogManager.getLogger(OaiPmhService.class);

    @Value("${recordsPerPage}")
    private int recordsPerPage;

    @Value("${identifiersPerPage}")
    private int identifiersPerPage;

    @Value("${resumptionTokenTTL}")
    private int resumptionTokenTTL;

    private RecordProvider recordProvider;

    private IdentifierProvider identifierProvider;

    private IdentifyProvider identifyProvider;

    private MetadataFormatsProvider metadataFormats;

    private SetsProvider setsProvider;

    public OaiPmhService(RecordProvider recordProvider, IdentifierProvider identifierProvider, IdentifyProvider identifyProvider, MetadataFormatsProvider metadataFormats, SetsProvider setsProvider) {
        super();
        this.recordProvider = recordProvider;
        this.identifierProvider = identifierProvider;
        this.identifyProvider = identifyProvider;
        this.metadataFormats = metadataFormats;
        this.setsProvider = setsProvider;
    }

    @PostConstruct
    private void init() {
        // Note that properties aren't available until after the construction of the bean
        LOG.info("Records per page: {}", recordsPerPage);
        LOG.info("Identifiers per page: {}", identifiersPerPage);
        LOG.info("Resumption token TTL: {}", resumptionTokenTTL);
    }

    /**
     * Return repository information according to OAI-PMH-protocol (see https://www.openarchives.org/OAI/openarchivesprotocol.html#Identify)
     * @return
     * @throws OaiPmhException
     */
    public String getIdentify(IdentifyRequest request) throws OaiPmhException {
        Identify responseObject = identifyProvider.provideIdentify();
        return serialize(responseObject.getResponse(request));
    }

    /**
     * Retrieve record information according to OAI-PMH protocol (see https://www.openarchives.org/OAI/openarchivesprotocol.html#GetRecord)
     * @param request GetRecord request containing all necessary parameters
     * @return record information in OAI-PMH (xml)
     * @throws OaiPmhException
     */
    public String getRecord(GetRecordRequest request) throws OaiPmhException {
        if (!metadataFormats.canDisseminate(request.getMetadataPrefix())) {
            throw new CannotDisseminateFormatException(request.getMetadataPrefix());
        }
        Record record = recordProvider.getRecord(request.getIdentifier());
        if (record == null) {
            throw new IdDoesNotExistException(request.getIdentifier());
        }
        GetRecord responseObject = new GetRecord(record);
        return serialize(responseObject.getResponse(request));
    }

    /**
     * Retrieve list of identifiers that match given filter parameters: metadata format, date between from and until and set.
     * When no identifiers were found then NoRecordsMatch error is returned. When resumption token is inside the request
     * then the method retrieves another page of results for ListIdentifiers verb starting from the point encoded in resumption token.
     *
     * @param request request containing all necessary parameters
     * @return list of identifiers matching the given filter parameters
     * @throws OaiPmhException
     */
    public String listIdentifiers(ListIdentifiersRequest request) throws OaiPmhException {
        ListIdentifiers responseObject = getListIdentifiersObject(request.getMetadataPrefix(),
                DateConverter.fromIsoDateTime(request.getFrom()),
                DateConverter.fromIsoDateTime(request.getUntil()),
                request.getSet(),
                request.getResumptionToken(),
                identifiersPerPage);
        if (! responseObject.getHeaders().isEmpty()) {
            return serialize(responseObject.getResponse(request));
        }
        OAIError error = new OAIError(ErrorCode.NO_RECORDS_MATCH, "No records found!");
        return serialize(error.getResponse(request));
    }

    /**
     * Prepare the ListIdentifiers object according to the specified parameters.
     *
     * @param metadataPrefix metadata prefix
     * @param from start date
     * @param until end date
     * @param set dataset identifier
     * @param resumptionToken resumption token (encoded)
     * @param pageSize page size
     * @return ListIdentifiers object containing max pageSize number of identifiers
     * @throws OaiPmhException
     */
    private ListIdentifiers getListIdentifiersObject(String metadataPrefix, Date from, Date until, String set, String resumptionToken, int pageSize) throws OaiPmhException {
        ListIdentifiers responseObject;
        if (resumptionToken != null) {
            ResumptionToken validated = validateResumptionToken(resumptionToken);
            responseObject = identifierProvider.listIdentifiers(validated, pageSize);
        } else {
            if (!metadataFormats.canDisseminate(metadataPrefix)) {
                throw new CannotDisseminateFormatException(metadataPrefix);
            }

            responseObject = identifierProvider.listIdentifiers(metadataPrefix, from, until, set, pageSize);
        }
        return responseObject;
    }

    /**
     * Retrieve list of sets
     *
     * @param request request containing all necessary parameters
     * @return list of sets
     * @throws OaiPmhException
     */
    public String listSets(ListSetsRequest request) throws OaiPmhException {
        ListSets responseObject;
        if (request.getResumptionToken() != null) {
            ResumptionToken validated = validateResumptionToken(request.getResumptionToken());
            responseObject = setsProvider.listSets(validated);
        } else {
            responseObject = setsProvider.listSets(DateConverter.fromIsoDateTime(request.getFrom()),
                    DateConverter.fromIsoDateTime(request.getUntil()));
        }
        if (! responseObject.getSets().isEmpty()) {
            return serialize(responseObject.getResponse(request));
        }
        OAIError error = new OAIError(ErrorCode.NO_SETS_MATCH, "No sets exist!");
        return serialize(error.getResponse(request));
    }

    /**
     * Retrieve list of records that match given filter parameters: metadata format, date between from and until and set.
     * When no records were found then NoRecordsMatch error is returned.
     *
     * @param request request containing all necessary parameters
     * @return list of records matching the given filter parameters
     * @throws OaiPmhException
     */
    public String listRecords(ListRecordsRequest request) throws OaiPmhException {
        ListIdentifiers identifiers = getListIdentifiersObject(request.getMetadataPrefix(),
                DateConverter.fromIsoDateTime(request.getFrom()),
                DateConverter.fromIsoDateTime(request.getUntil()),
                request.getSet(),
                request.getResumptionToken(),
                recordsPerPage);
        if (! identifiers.getHeaders().isEmpty()) {
            ListRecords responseObject = recordProvider.listRecords(identifiers.getHeaders());
            if (! responseObject.getRecords().isEmpty()) {
                responseObject.setResumptionToken(identifiers.getResumptionToken());
                return serialize(responseObject.getResponse(request));
            }
        }
        OAIError error = new OAIError(ErrorCode.NO_RECORDS_MATCH, "No records found!");
        return serialize(error.getResponse(request));
    }

    /**
     * Validate resumption token passed by the client. The base64 string is decoded and is checked against the expiration date.
     * When resumption token is incorrect then BadResumptionToken error is returned.
     *
     * @param resumptionToken resumption token
     * @return decoded resumption token ready to be used by the internal request to IdentifierProvider
     * @throws BadResumptionToken
     */
    private ResumptionToken validateResumptionToken(String resumptionToken) throws BadResumptionToken {
        ResumptionToken temporaryToken;
        try {
            temporaryToken = ResumptionTokenHelper.decodeResumptionToken(resumptionToken);
        } catch (IllegalArgumentException e) {
            throw new BadResumptionToken("Resumption token " + resumptionToken + " is not correct.");
        }
        if (new Date().after(temporaryToken.getExpirationDate())) {
            throw new BadResumptionToken("Resumption token expired at " + temporaryToken.getExpirationDate());
        }
        return temporaryToken;
    }

    @PreDestroy
    private void close() {
        LOG.info("Closing OAI-PMH service...");
        identifierProvider.close();
        recordProvider.close();
        LOG.info("OAI-PMH service closed.");
    }

    public String listMetadataFormats(ListMetadataFormatsRequest request) throws OaiPmhException {
        if (request.getIdentifier() != null) {
            recordProvider.checkRecordExists(request.getIdentifier());
        }
        ListMetadataFormats responseObject = metadataFormats.listMetadataFormats();
        if (! responseObject.getMetadataFormats().isEmpty()) {
            return serialize(responseObject.getResponse(request));
        }
        OAIError error = new OAIError(ErrorCode.NO_METADATA_FORMATS, "There are no metadata formats available.");
        return serialize(error.getResponse(request));
    }
}
