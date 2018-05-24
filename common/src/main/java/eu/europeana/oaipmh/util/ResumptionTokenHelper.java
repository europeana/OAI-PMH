package eu.europeana.oaipmh.util;

import eu.europeana.oaipmh.model.ResumptionToken;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

/**
 * Helper class for managing resumption tokens. It is used to encode and decode information into base64 string that is returned to the client.
 * Generally token should have the following format:
 *
 * FROM|UNTIL|SET|FORMAT|EXPIRATION_TIME|COMPLETE_LIST_SIZE|CURSOR|CURSOR_MARK
 *
 *  where:
 *
 * FROM - start date used for filtering identifiers (ListIdentifiers) or records (ListRecords) specified as from parameter; token generated for ListSets will have this part empty
 * UNTIL - end date used for filtering identifiers (ListIdentifiers) or records (ListRecords) specified as until parameter; token generated for ListSets will have this part empty
 * SET - identifier of the dataset used for filtering identifiers (ListIdentifiers) or records (ListRecords) specified as set parameter; token generated for ListSets will have this part empty
 * FORMAT - matadata format used for filtering identifiers (ListIdentifiers) or records (ListRecords) specified as metadataPrefix parameter; token generated for ListSets will have this part empty
 * EXPIRATION_TIME - date (as miliseconds) when this resumption token expires
 * COMPLETE_LIST_SIZE - number of all elements that should be returned for the request (valid only for ListSets request, tokens for other requests will have this part empty)
 * CURSOR - number of already retrieved identifiers (ListIdentifiers), records (ListRecords) or sets (ListSets)
 * CURSOR_MARK - cursor mark used by Solr to continue the query, valid only for ListIdentifiers and ListRecords requests
 *
 */
public class ResumptionTokenHelper {
    private static final String TOKEN_SEPARATOR = "|";

    private static final String TOKEN_TEMPLATE = "%s|%s|%s|%s|%s|%s|%s|%s";

    private static final byte FROM_INDEX = 0;
    private static final byte UNTIL_INDEX = 1;
    private static final byte SET_INDEX = 2;
    private static final byte FORMAT_INDEX = 3;
    private static final byte EXPIRATION_TIME_INDEX = 4;
    private static final byte COMPLETE_LIST_SIZE_INDEX = 5;
    private static final byte CURSOR_INDEX = 6;
    private static final byte CURSOR_MARK_INDEX = 7;

    private ResumptionTokenHelper() {}

    /**
     * Creates the resumption token object that consists of the specified parameters. Some of them are optional according to the request that
     * the created token is used for.
     *
     * @param from start date (optional)
     * @param until end date (optional)
     * @param set set identifier (optional)
     * @param format metadata format (optional)
     * @param expirationDate token expiration date (mandatory)
     * @param completeListSize total number of results (mandatory)
     * @param cursor number of already retrieved results (mandatory)
     * @param nextCursorMark next cursor mark that will be used for retrieving next page of results from Solr (optional)
     * @return resumption token object that contains encoded token string
     */
    public static ResumptionToken createResumptionToken(String from,
                                                        String until,
                                                        String set,
                                                        String format,
                                                        Date expirationDate,
                                                        long completeListSize,
                                                        long cursor,
                                                        String nextCursorMark) {
        String tokenEncoded = encodeToken(from, until, set, format, expirationDate, completeListSize, cursor, nextCursorMark);
        return new ResumptionToken(tokenEncoded, completeListSize, expirationDate, cursor);
    }

    /**
     * Creates the resumption token object that consists of the specified parameters. Some of them are optional according to the request that
     * the created token is used for.
     *
     * @param expirationDate token expiration date (mandatory)
     * @param completeListSize total number of results (mandatory)
     * @param cursor number of already retrieved results (mandatory)
     * @return resumption token object that contains encoded token string
     */
    public static ResumptionToken createResumptionToken(Date expirationDate,
                                                        long completeListSize,
                                                        long cursor) {
        return createResumptionToken(null, null, null, null, expirationDate, completeListSize, cursor, null);
    }

    /**
     * Returns empty string when given value is null or the specified value otherwise.
     *
     * @param value value to be checked and returned
     * @return value or empty string when value is null
     */
    private static String getTokenPart(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }


    /**
     * Prepare and encode the token string with Base64.
     *
     * @param from start date
     * @param until end date
     * @param set set identifier
     * @param format metadata format
     * @param expirationDate token expiration date
     * @param completeListSize total number of results
     * @param cursor number of already retrieved results
     * @param nextCursorMark next cursor mark that will be used for retrieving next page of results from Solr
     * @return token string encoded with Base64
     */
    private static String encodeToken(String from,
                                      String until,
                                      String set,
                                      String format,
                                      Date expirationDate,
                                      long completeListSize,
                                      long cursor,
                                      String nextCursorMark) {
        String token = String.format(TOKEN_TEMPLATE,
                getTokenPart(from),
                getTokenPart(until),
                getTokenPart(set),
                getTokenPart(format),
                expirationDate.getTime(),
                completeListSize,
                cursor,
                nextCursorMark);
        return Base64.getUrlEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes the given token (encoded with Base64) and retrieves information on expiration date, cursor and complete list size.
     * The value stored in this token is in decoded form and CANNOT be used as a part of the response for OAI-PMH request.
     * However this gives possibility to retrieve all necessary information (from, until, set, format, expiration date, complete list size,
     * cursor and next cursor mark).
     *
     * @param base64EncodedToken token encoded with Base64
     * @return temporary resumption token object with decoded value, expiration date, cursor and complete list size.
     */
    public static ResumptionToken decodeResumptionToken(String base64EncodedToken) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(base64EncodedToken), StandardCharsets.UTF_8);
            String[] parts = tokenize(decoded);
            Date expirationDate = new Date(Long.valueOf(parts[EXPIRATION_TIME_INDEX]));
            long cursor = Long.parseLong(parts[CURSOR_INDEX]);
            long completeListSize = Long.parseLong(parts[COMPLETE_LIST_SIZE_INDEX]);
            return new ResumptionToken(decoded, completeListSize, expirationDate, cursor);
        } catch (Exception e) {
            // in case of any exception we assume there is something wrong with the resumption token being decoded
            throw new IllegalArgumentException();
        }
    }

    /**
     * Splits the resumption token into parts and checks whether there are 8 of them. If not IllegalArgumentException will be thrown
     *
     * @param token resumption token in decoded form
     * @return array of parts
     */
    private static String[] tokenize(String token) {
        String[] parts = StringUtils.splitByWholeSeparatorPreserveAllTokens(token, TOKEN_SEPARATOR);
        if (parts.length == 8) {
            // token must have 8 parts
            return parts;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Get value of FROM part from the resumption token.
     *
     * @param token resumption token in decoded form
     * @return value of FROM part
     */
    public static String getFrom(String token) {
        return tokenize(token)[FROM_INDEX];
    }

    /**
     * Get value of UNTIL part from the resumption token.
     *
     * @param token resumption token in decoded form
     * @return value of UNTIL part
     */
    public static String getUntil(String token) {
        return tokenize(token)[UNTIL_INDEX];
    }

    /**
     * Get value of SET part from the resumption token.
     *
     * @param token resumption token in decoded form
     * @return value of SET part
     */
    public static String getSet(String token) {
        return tokenize(token)[SET_INDEX];
    }

    /**
     * Get value of FORMAT part from the resumption token.
     *
     * @param token resumption token in decoded form
     * @return value of FORMAT part
     */
    public static String getFormat(String token) {
        return tokenize(token)[FORMAT_INDEX];
    }

    /**
     * Get value of EXPIRATION_TIME part from the resumption token.
     *
     * @param token resumption token in decoded form
     * @return value of EXPIRATION_TIME part
     */
    public static Date getExpirationDate(String token) {
        return new Date(Long.valueOf(tokenize(token)[EXPIRATION_TIME_INDEX]));
    }

    /**
     * Get value of COMPLETE_LIST_SIZE part from the resumption token.
     *
     * @param token resumption token in decoded form
     * @return value of COMPLETE_LIST_SIZE part
     */
    public static long getCompleteListSize(String token) {
        return Long.parseLong(tokenize(token)[COMPLETE_LIST_SIZE_INDEX]);
    }

    /**
     * Get value of CURSOR part from the resumption token.
     *
     * @param token resumption token in decoded form
     * @return value of CURSOR part
     */
    public static long getCursor(String token) {
        return Long.parseLong(tokenize(token)[CURSOR_INDEX]);
    }

    /**
     * Get value of CURSOR_MARK part from the resumption token.
     *
     * @param token resumption token in decoded form
     * @return value of CURSOR_MARK part
     */
    public static String getCursorMark(String token) {
        return tokenize(token)[CURSOR_MARK_INDEX];
    }
}
