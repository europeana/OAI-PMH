package eu.europeana.oaipmh.client;

public class Constants {

    //verb
    public static final String GET_RECORD_VERB          = "GetRecord";
    public static final String LIST_IDENTIFIERS_VERB    = "ListIdentifiers";
    public static final String LIST_RECORDS_VERB        = "ListRecords";

    // Zip constants
    public static final String ZIP_EXTENSION            = ".zip";
    public static final String PATH_SEPERATOR           = "/";
    public static final String EXTENSION                = ".xml";
    public static final String BASE_URL                 = "http://data.europeana.eu/item/";

    // constants for parsing XML Response
    public static final String HEADER_TAG               = "header";
    public static final String MEATADATA_TAG            = "metadata";
    public static final String RECORD_TAG               = "record";
    public static final String IDENTIFIER_TAG           = "identifier";
    public static final String SETSPEC_TAG              = "setSpec";
    public static final String DATESTAMP_TAG            = "datestamp";
    public static final String RESUMPTIONTOKEN_TAG      = "resumptionToken";
    public static final String COMPLETELISTSIZE_TAG     = "completeListSize";
    public static final String EXPIRATIONDATE_TAG       = "expirationDate";
    public static final String CURSOR_TAG               = "cursor";
    public static final String DATE_FORMAT              = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    //Boolean Values
    public static final String TRUE                     = "true";
    public static final String FALSE                    = "false";
}

