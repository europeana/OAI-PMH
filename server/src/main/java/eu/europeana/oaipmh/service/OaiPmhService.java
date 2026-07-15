package eu.europeana.oaipmh.service;

import eu.europeana.oaipmh.config.OaiPmhSettings;
import eu.europeana.oaipmh.model.*;
import eu.europeana.oaipmh.model.Record;
import eu.europeana.oaipmh.model.impl.GetRecordImpl;
import eu.europeana.oaipmh.model.metadata.MetadataFormatsProvider;
import eu.europeana.oaipmh.model.request.*;
import eu.europeana.oaipmh.model.response.OAIResponse;
import eu.europeana.oaipmh.service.exception.*;
import eu.europeana.oaipmh.util.DateConverter;
import eu.europeana.oaipmh.util.ResumptionTokenHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static eu.europeana.oaipmh.service.exception.ErrorConstants.*;

/**
 *
 *
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@Service
public class OaiPmhService {

    private static final Logger LOG = LogManager.getLogger(OaiPmhService.class);

    @Resource
    OaiPmhSettings settings;

    private RecordProvider recordProvider;

    private IdentifierProvider identifierProvider;

    private IdentifyProvider identifyProvider;

    private MetadataFormatsProvider metadataFormats;

    private SetsProvider setsProvider;

    public OaiPmhService(RecordProvider recordProvider
                       , IdentifierProvider identifierProvider
                       , IdentifyProvider identifyProvider
                       , MetadataFormatsProvider metadataFormats
                       , SetsProvider setsProvider) {
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
        LOG.info("Records per page: {}", settings.getRecordsPerPage());
        LOG.info("Identifiers per page: {}", settings.getIdentifiersPerPage());
        LOG.info("Resumption token TTL: {}", settings.getResumptionTokenTTL());
    }

    /**
     * Return repository information according to OAI-PMH-protocol (see https://www.openarchives.org/OAI/openarchivesprotocol.html#Identify)
     * @return
     * @throws OaiPmhException
     */
    public OAIResponse getIdentify(IdentifyRequest req) throws OaiPmhException {
        return new OAIResponse(req, identifyProvider.provideIdentify());
    }

    public OAIResponse listMetadataFormats(ListMetadataFormatsRequest req) throws OaiPmhException {
        if (req.getIdentifier() != null) {
            recordProvider.checkRecordExists(req.getIdentifier());
        }
        ListMetadataFormats responseObject = metadataFormats.listMetadataFormats();
        if (! responseObject.getMetadataFormats().isEmpty()) {
            return new OAIResponse(req, responseObject);
        }
        return new OAIResponse(req, new OAIError(ErrorCode.NO_METADATA_FORMATS
                                               , NO_METADATA_FORMATS_MSG));
    }

    /**
     * Retrieve record information according to OAI-PMH protocol (see https://www.openarchives.org/OAI/openarchivesprotocol.html#GetRecord)
     * @param request GetRecord request containing all necessary parameters
     * @return record information in OAI-PMH (xml)
     * @throws OaiPmhException
     */
    public OAIResponse getRecord(GetRecordRequest req) throws OaiPmhException {
        if (!metadataFormats.canDisseminate(req.getMetadataPrefix())) {
            throw new CannotDisseminateFormatException(req.getMetadataPrefix());
        }
        Record record = recordProvider.getRecord(req.getIdentifier());
        if (record == null) {
            throw new IdDoesNotExistException(req.getIdentifier());
        }
        return new OAIResponse(req, new GetRecordImpl(record));
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
    public OAIResponse listIdentifiers(ListIdentifiersRequest req) 
            throws OaiPmhException {
        ListIdentifiers responseObject = getListIdentifiersObject(
                req.getMetadataPrefix(),
                DateConverter.fromIsoDateTime(req.getFrom()),
                DateConverter.fromIsoDateTime(req.getUntil()),
                req.getSet(),
                req.getResumptionToken(),
                settings.getIdentifiersPerPage());
        OAIPMHVerb verb = (!responseObject.isEmpty() ? 
            responseObject : new OAIError(ErrorCode.NO_RECORDS_MATCH
                                        , NO_RECORDS_MATCH_MSG));
        return new OAIResponse(req, verb);
    }

    /**
     * Retrieve list of sets
     *
     * @param request request containing all necessary parameters
     * @return list of sets
     * @throws OaiPmhException
     */
    public OAIResponse listSets(ListSetsRequest req) 
            throws OaiPmhException {
        ListSets responseObject;
        if (req.getResumptionToken() != null) {
            ResumptionToken validated = validateResumptionToken(req.getResumptionToken());
            responseObject = setsProvider.listSets(validated);
        } else {
            responseObject = setsProvider.listSets(
                DateConverter.fromIsoDateTime(req.getFrom()),
                DateConverter.fromIsoDateTime(req.getUntil()));
        }
        try ( responseObject ) {
            if (! responseObject.isEmpty()) {
                return new OAIResponse(req, responseObject);
            }
        }
        catch (Exception e) { throw new OaiPmhException(e); }

        return new OAIResponse(req, new OAIError(ErrorCode.NO_SETS_MATCH
                                               , NO_SETS_MATCH_MSG));
    }

    /**
     * Retrieve list of records that match given filter parameters: metadata format, date between from and until and set.
     * When no records were found then NoRecordsMatch error is returned.
     *
     * @param req request containing all necessary parameters
     * @return list of records matching the given filter parameters
     * @throws OaiPmhException
     */
    public OAIResponse listRecords(ListRecordsRequest req) throws OaiPmhException {
        ListIdentifiers identifiers = getListIdentifiersObject(
                req.getMetadataPrefix(),
                DateConverter.fromIsoDateTime(req.getFrom()),
                DateConverter.fromIsoDateTime(req.getUntil()),
                req.getSet(),
                req.getResumptionToken(),
                settings.getRecordsPerPage());
        List<String> ids;
        try ( identifiers ) {
            ids = identifiers.stream().map(t -> t.getIdentifier())
                                      .toList();
        }
        catch ( OaiPmhException e ) { throw e; }
        catch (Exception e        ) { throw new OaiPmhException(e); }

        if (!ids.isEmpty()) {
            ListRecords responseObject = recordProvider.listRecords(
                    ids, identifiers.getResumptionToken());
          //  if (!responseObject.isEmpty()) {
                return new OAIResponse(req, responseObject);
         //   }
        }
        return new OAIResponse(req, new OAIError(ErrorCode.NO_RECORDS_MATCH
                                                 , NO_RECORDS_MATCH_MSG));
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
            throw new BadResumptionToken(msg(BAD_RESUMPTION_TOKEN_MSG
                                           , resumptionToken));
        }
        if (new Date().after(temporaryToken.getExpirationDate())) {
            throw new BadResumptionToken(msg(BAD_RESUMPTION_TOKEN_EXPIRED_MSG
                                           , temporaryToken.getExpirationDate()));
        }
        return temporaryToken;
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
    private ListIdentifiers getListIdentifiersObject(
            String metadataPrefix, Date from, Date until, String set
          , String resumptionToken, int pageSize) throws OaiPmhException {

        if (resumptionToken != null) {
            ResumptionToken validated = validateResumptionToken(resumptionToken);
            return identifierProvider.listIdentifiers(validated, pageSize);
        } 

        if (!metadataFormats.canDisseminate(metadataPrefix)) {
            throw new CannotDisseminateFormatException(metadataPrefix);
        }

        return identifierProvider.listIdentifiers(metadataPrefix, from, until
                                                , set, pageSize);
    }

    @PreDestroy
    private void close() {
        LOG.info("Closing OAI-PMH service...");
        identifierProvider.close();
        recordProvider.close();
        LOG.info("OAI-PMH service closed.");
    }
}
