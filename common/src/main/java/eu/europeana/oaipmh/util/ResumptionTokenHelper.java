package eu.europeana.oaipmh.util;

import eu.europeana.oaipmh.model.ResumptionToken;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Helper class for managing resumption tokens. It is used to encode and decode information into base64 string that is returned to the client.
 */
public class ResumptionTokenHelper {
    private static final String TOKEN_SEPARATOR = "___";

    private static final String FILTER_SEPARATOR = "&&&";

    private ResumptionTokenHelper() {}

    /**
     * Creates resumption token object that can be used to prepare the response for the request
     *
     * @param nextCursorMark next cursor mark from Solr
     * @param completeListSize complete list size
     * @param expirationDate expiration date of the resumption token
     * @param cursor cursor of the current request
     * @param filterQuery filter query used for this resumption token, must be the same when we want to use the nextCursorMark for the next page
     * @return resumption token object which contains token encoded with Base64 and containing information on expiration date, cursor and next cursor mark
     */
    public static ResumptionToken createResumptionToken(String nextCursorMark, long completeListSize, Date expirationDate, long cursor, List<String> filterQuery) {
        String encodedCursorMark = encodeCursorMark(nextCursorMark, expirationDate, cursor, filterQuery);
        return new ResumptionToken(encodedCursorMark, completeListSize, expirationDate, cursor, filterQuery);
    }

    /**
     * Encode cursor from solr together with expiration date and progress cursor using base64
     *
     * @param nextCursorMark cursor mark from solr
     * @param expirationDate token expiration date
     * @param cursor progress cursor
     * @return encoded token containing all necessary information
     */
    private static String encodeCursorMark(String nextCursorMark, Date expirationDate, long cursor, List<String> filterQuery) {
        StringBuilder filter = new StringBuilder();
        for (int i = 0; i < filterQuery.size(); i++) {
            filter.append(filterQuery.get(i));
            if (i < filterQuery.size() - 1) {
                filter.append(FILTER_SEPARATOR);
            }
        }
        return Base64.getUrlEncoder().encodeToString(String.valueOf(filter + TOKEN_SEPARATOR + expirationDate.getTime() +
                TOKEN_SEPARATOR + cursor + TOKEN_SEPARATOR + nextCursorMark).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes the given token (encoded with Base64) and retrieves information on expiration date, cursor and next cursor mark.
     * The returned resumption token CANNOT be used as a part of the response for OAI-PMH request.
     *
     * @param base64EncodedToken token encoded with Base64
     * @return temporary resumption token object with decoded next cursor mark (that can be used by Solr directly), expiration date and cursor
     * but invalid complete list size.
     */
    public static ResumptionToken decodeResumptionToken(String base64EncodedToken) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(base64EncodedToken), StandardCharsets.UTF_8);
            String[] parts = decoded.split(TOKEN_SEPARATOR);
            if (parts.length == 4) {
                String[] filterQuery = parts[0].split(FILTER_SEPARATOR);
                Date expirationDate = new Date(Long.valueOf(parts[1]));
                long cursor = Long.parseLong(parts[2]);
                String cursorMark = parts[3];
                return new ResumptionToken(cursorMark, -1, expirationDate, cursor, Arrays.asList(filterQuery));
            }
        } catch (Exception e) {
            // in case of any exception we assume there is something wrong with the resumption token being decoded
            throw new IllegalArgumentException();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Creates a simple version of resumption token which does not use cursor mark. It consists of 3 elements in the following order:
     * complete list size, expiration date and cursor. They are concatenated and encoded with Base64.
     *
     * @param completeListSize complete list size
     * @param expirationDate the date this token expires
     * @param cursor number of items retrieved so far
     * @return resumption token object
     */
    public static ResumptionToken createSimpleResumptionToken(long completeListSize, Date expirationDate, long cursor) {
        String encodedValue = encodeSimpleResumptionToken(completeListSize, expirationDate, cursor);
        return new ResumptionToken(encodedValue, completeListSize, expirationDate, cursor, null);
    }

    /**
     * Decodes the simple resumption token. The decoded values (complete list size, expiration date and cursor) will be set inside the object.
     * The returned object CANNOT be used in the response to the client.
     *
     * @param base64EncodedToken encoded token
     * @return resumption token object filled with values retrieved from the encoded token
     * @throws IllegalArgumentException
     */
    public static ResumptionToken decodeSimpleResumptionToken(String base64EncodedToken) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(base64EncodedToken), StandardCharsets.UTF_8);
            String[] parts = decoded.split(TOKEN_SEPARATOR);
            if (parts.length == 3) {
                Long completeListSize = Long.valueOf(parts[0]);
                Date expirationDate = new Date(Long.valueOf(parts[1]));
                long cursor = Long.parseLong(parts[2]);
                return new ResumptionToken(null, completeListSize, expirationDate, cursor, null);
            }
        } catch (Exception e) {
            // in case of any exception we assume there is something wrong with the resumption token being decoded
            throw new IllegalArgumentException();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Encode the values of the resumption token into one Base64 string.
     *
     * @param completeListSize complete list size
     * @param expirationDate the date the token expires
     * @param cursor number of items returned so far
     * @return Base64 encoded string
     */
    private static String encodeSimpleResumptionToken(long completeListSize, Date expirationDate, long cursor) {
        return Base64.getUrlEncoder().encodeToString(String.valueOf(completeListSize + TOKEN_SEPARATOR + expirationDate.getTime() +
                TOKEN_SEPARATOR + cursor).getBytes(StandardCharsets.UTF_8));
    }
}
