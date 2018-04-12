package eu.europeana.oaipmh.util;

import eu.europeana.oaipmh.model.ResumptionToken;

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
        return Base64.getUrlEncoder().encodeToString(String.valueOf(filter + TOKEN_SEPARATOR + expirationDate.getTime() + TOKEN_SEPARATOR + cursor + TOKEN_SEPARATOR + nextCursorMark).getBytes());
    }

    /**
     * Decodes the given token (encoded with Base64) and retrieves information on expiration date, cursor and next cursor mark.
     * The returned resumption token CANNOT be used as a part of the response for OAI-PMH request.
     *
     * @param base64EncodedToken token encoded with Base64
     * @return temporary resumption token object with decoded next cursor mark (that can be used by Solr directly), expiration date and cursor
     * but invalid complete list size.
     */
    public static ResumptionToken decodeResumptionToken(String base64EncodedToken) throws IllegalArgumentException {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(base64EncodedToken));
            String[] parts = decoded.split(TOKEN_SEPARATOR);
            if (parts.length == 4) {
                String[] filterQuery = parts[0].split(FILTER_SEPARATOR);
                Date expirationDate = new Date(Long.valueOf(parts[1]));
                long cursor = Long.valueOf(parts[2]);
                String cursorMark = parts[3];
                return new ResumptionToken(cursorMark, -1, expirationDate, cursor, Arrays.asList(filterQuery));
            }
        } catch (Exception e) {
            // in case of any exception we assume there is something wrong with the resumption token being decoded
            throw new IllegalArgumentException();
        }
        throw new IllegalArgumentException();
    }
}
