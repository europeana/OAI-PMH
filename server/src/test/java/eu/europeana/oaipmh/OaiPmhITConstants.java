package eu.europeana.oaipmh;

public class OaiPmhITConstants {

    protected static final String METADATA_FORMAT_PREFIX = "format_prefix";
    protected static final String METADATA_FORMAT_SCHEMA = "format_schema";
    protected static final String METADATA_FORMAT_NAMESPACE = "format_namespace";
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

}
