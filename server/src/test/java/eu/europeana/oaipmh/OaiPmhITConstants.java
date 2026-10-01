package eu.europeana.oaipmh;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class OaiPmhITConstants {

    protected static final String METADATA_FORMAT_PREFIX = "edm";
    protected static final String METADATA_FORMAT_SCHEMA = "http://www.europeana.eu/schemas/edm/EDM.xsd";
    protected static final String METADATA_FORMAT_NAMESPACE = " http://www.europeana.eu/schemas/edm/";
    protected static final String EU_EUROPEANA_OAIPMH_MODEL_METADATA_XML2_EDMCONVERTER = "eu.europeana.oaipmh.model.metadata.XML2EDMConverter";

    protected static final String REPOSITORY_NAME="Europeana OAI Endpoint v2.0";
    protected static final String BASE_URL="https://oai.europeana.eu/oai";
    protected static final String PROTOCOL_VERSION="2.0";
    protected static final String EARLIEST_DATESTAMP="1970-01-01T00:00:00Z";
    protected static final String DELETED_RECORD="no";
    protected static final String GRANULARITY="YYYY-MM-DDThh:mm:ssZ";
    protected static final String ADMIN_EMAIL="api@europeana.eu";
    protected static final String COMPRESSION="gzip";

    protected static final long RESUMPTION_TOKEN_TTL= 86400000;
    protected static final int IDENTIFIERS_PER_PAGE= 300;

    protected static final String DATE_1           = "2017-08-02T22:00:00Z";
    protected static final String DATE_2           = "2018-08-03T12:16:21Z";
    protected static final String DATE_3           = "2019-08-03T15:25:23Z";
    protected static final String SET_1            = "2064125";
    protected static final String SET_2            = "08506";
    protected static final String SET_3            = "14";
    protected static final String SET_4            = "401";
    protected static final String SET_5            = "876";
    protected static final long COMPLETE_LIST_SIZE = 500;
    protected static final String METADATA_FORMAT = "edm";

    protected static final String RECORD_ID_401_1      = "/" + SET_4 +"/item_" + 1;
    protected static final String RECORD_ID_401_3      = "/" + SET_4 +"/item_" + 3;
    protected static final String RECORD_ID_2064125_4  = "/" + SET_1 +"/item_" + 4;
    protected static final String RECORD_ID_2064125_1  = "/" + SET_1 +"/item_" + 1;
    protected static final String RECORD_ID_08506_1    = "/" + SET_2 +"/item_" + 1;
    protected static final String RECORD_ID_08506_7    = "/" + SET_2 +"/item_" + 7;
    protected static final String RECORD_ID_876_1      = "/" + SET_5 +"/item_" + 1;
    protected static final String RECORD_ID_876_2      = "/" + SET_5 +"/item_" + 2;


    // oai pmh request factory constants
    protected static final String VALID_REQUEST = "verb=ListIdentifiers&metadataPrefix=edm";
    protected static final String NO_VERB_REQUEST = "metadataPrefix=edm&set=abc";
    protected static final String EMPTY_PARAMETER_VALUE_REQUEST = "verb=ListIdentifiers&set=";
    protected static final String EMPTY_PARAMETER_NAME_REQUEST = "verb=ListIdentifiers&=";
    protected static final String INVALID_PARAMETER_NAME_REQUEST = "verbs=ListIdentifiers";
    protected static final String MULTI_PARAMETER_REQUEST_1 = "verb=ListIdentifiers&set=abc&set=vbn";
    protected static final String MULTI_PARAMETER_REQUEST_2 = "verb=ListIdentifiers&set=abc,vbn";
    protected static final String UNSUPPORTED_VERB_REQUEST = "verb=XYZ";
    protected static final String UNSUPPORTED_VERB = "GetIdentifiers";
    protected static final String IDENTIFY_REQUEST = "verb=Identify";
    protected static final String LIST_IDENTIFIERS_GENERAL_REQUEST = "verb=ListIdentifiers&metadataPrefix=edm&set=ABC";
    protected static final String LIST_IDENTIFIERS_NO_MANDATORY_REQUEST = "verb=ListIdentifiers&set=ABC";
    protected static final String GET_RECORD_GENERAL_REQUEST = "verb=GetRecord&metadataPrefix=edm&identifier=ABC";
    protected static final String GET_RECORD_NO_MANDATORY_REQUEST = "verb=GetRecord&metadataPrefix=edm";
    protected static final String EDM_FORMAT = "edm";
    protected static final String SET_NAME = "ABC";
    protected static final String VERB_LIST_IDENTIFIERS_RESUMPTION_TOKEN_REQUEST = "verb=ListIdentifiers&resumptionToken=JHJHGHSAGHSJGAJ";
    protected static final String RESUMPTION_TOKEN = "JHJHGHSAGHSJGAJ";


    protected static final String LIST_IDENTIFIERS_TOKEN = "ZWRtX2RhdGFzZXROYW1lOiIyMDQ4NDMyX0FnX0RFX0REQl9CSU5FXzk5OTAwNjc4Il9fXzE1MjI0ODM3NzI5MDhfX18wX19fQW9KMnI4N2oxdDBDUHc4dk1qQTBPRFF6TWk5cGRHVnRYMFZJTlZKUFJrNVlRalJJVDFOWldqUkJVazgyUjBSQ1NrVkhUMW96V1VnMQ==";
    protected static final String LIST_IDENTIFIERS_CORRUPTED_TOKEN = "ZWRtX19fQW9KMnI4N2oxdDBDUHc4dk1qQTBPRFF6TWk5cGRHVnRYMFZJTlZKUFJrNVlRaljBSQ1NrVkhUMW96V1VnMQ==";
    protected static final String LIST_RECORDS_TOKEN = "fHw5MjAwNTA5fGVkbXwxNTI3NTgzNjYwMjQ2fDUxfDB8QW9KOW9vV2h4TjBDUHdjdk9USXdNRFV3T1M5dVlYTnNiM1p1WlY5emJHbHJaVjlqWlhScGJtcGxNRE0yWDJwd1p3PT0=";
    protected static final String LIST_RECORDS_CORRUPTED_TOKEN = "ZWRtX19fQW9KMnI4N2oxdDBDUHc4dk1qQTBPRFF6TWk5cGRHVnRYMFZJTlZKUFJrNVlRaljBSQ1NrVkhUMW96V1VnMQ==";
    protected static final String MEDIA_TYPE_TEXT_XML = "text/xml;charset=UTF-8";
    protected static final String MEDIA_TYPE_APPLICATION_XML = "application/xml;charset=UTF-8";
    protected static final String OAI_ENDPOINT = "/oai?verb=Identify";


    /**
     * For now we have basic bean setup, as for actual metadata then we are required to save in all the interlinked collections
     * Once we switch to Record API v3 , it would be much easier
     * @param europeanaId id of te bean
     * @param timestampCreated when it is created
     * @param timestampUpdated updated time
     * @return
     */
    protected static FullBean getFullBean(String europeanaId, String timestampCreated, String timestampUpdated) {
        final FullBeanImpl fullBean = new FullBeanImpl();
        fullBean.setEuropeanaId(new ObjectId(UUID.randomUUID().toString().replace("-", "").substring(0, 24)));
        fullBean.setLanguage(new String[]{"en", "fr"});
        fullBean.setCountry(
                new String[]{"Nederland", "België"});
        fullBean.setYear(new String[]{"2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029"});
        fullBean.setAbout(europeanaId);
        fullBean.setType("TEXT");
        fullBean.setTimestampCreated(Date.from(Instant.parse(timestampCreated)));
        fullBean.setTimestampUpdated(Date.from(Instant.parse(timestampUpdated)));
        fullBean.setProvider(new String[]{"Pro1", "Prov2", "Prov3", "Prov4", "Prov5", "Prov6", "Prov7", "Prov8"});
        fullBean.setUserTags(new String[]{"UsrTag1", "UsrTag2", "UsrTag3", "UsrTag4", "UsrTag5", "UsrTag6", "UsrTag7", "UsrTag8"});
        fullBean.setEuropeanaCollectionName(new String[]{"Collection1", "Collection2", "Collection3", "Collection4", "Collection5"});
        fullBean.setEuropeanaCompleteness(100);

        return fullBean;
    }
}
