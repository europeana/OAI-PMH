package eu.europeana.oaipmh.service.exception;

public interface ErrorConstants {


    public static final String BAD_ARGUMENT_VERB_MSG = "Unsupported verb.";
    
    public static final String BAD_ARGUMENT_VERB_MISSING_MSG = "Verb parameter is missing...";

    public static final String BAD_VERB_INVALID_MSG = "Verb \"%s\" is invalid!";
    
    public static final String BAD_VERB_MISSING_MSG = "Verb is missing.";

    public static final String INVALID_SET_ID_MSG = "Set id is invalid";

    public static final String NO_METADATA_FORMATS_MSG 
        = "There are no metadata formats available.";

    public static final String NO_RECORDS_MATCH_MSG 
        = "No records found!";

    public static final String NO_SETS_MATCH_MSG 
        = "No sets exist!";

    public static final String ID_DOES_NOT_EXIST_MSG 
        = "Record with identifier %s not found!";

    public static final String BAD_METHOD_MSG 
        = "Method %s is not allowed.";

    public static final String BAD_ARGUMENT_MSG 
        = "Required parameter %s is missing";
    
    public static final String BAD_RESUMPTION_TOKEN_MSG
        = "Resumption token %s is not correct.";

    public static final String BAD_RESUMPTION_TOKEN_EXPIRED_MSG 
        = "Resumption token expired at %s";

    public static final String SERIALIZATION_MSG 
        = "Error serializing data: %s";

    public static String msg(String template, Object... args) {
        return ( args.length == 0 ? template 
                                  : String.format(template, args) );
    }
}
