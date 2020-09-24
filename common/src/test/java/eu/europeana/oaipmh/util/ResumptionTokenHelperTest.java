package eu.europeana.oaipmh.util;

import eu.europeana.oaipmh.model.ResumptionToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ResumptionTokenHelperTest {
    private static final String INCORRECT_TOKEN = "AHGVHFGYDUSJKSBDSBHSVDJSVDYUASAHJISAKBB767873213HVHGVGH";

    private static final String INCORRECT_DECODED_TOKEN = "2017-08-16T08:03:31Z|2017-08-16T08:03:31Z|34455|||";

    private static final String MANDATORY_PARTS_TOKEN = "||||1232341234|1234|300|";

    private static final String NEXT_CURSOR_MARK = "ABC";

    private static final long COMPLETE_LIST_SIZE = 1234L;

    private static final long CURSOR = 300;

    private static final long TTL = 84600L;

    private static final String FROM = "2017-08-16T08:03:31Z";

    private static final String UNTIL = "2017-08-17T08:03:31Z";

    private static final String SET = "92098";

    private static final String FORMAT = "edm";

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeIncorrectToken() {
        ResumptionTokenHelper.decodeResumptionToken(INCORRECT_TOKEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPartFromIncorrectToken() {
        ResumptionTokenHelper.getCompleteListSize(INCORRECT_DECODED_TOKEN);
    }

    @Test
    public void testTokenWithEmptyParts() {
        assertTrue(ResumptionTokenHelper.getFrom(MANDATORY_PARTS_TOKEN).isEmpty());
        assertTrue(ResumptionTokenHelper.getUntil(MANDATORY_PARTS_TOKEN).isEmpty());
        assertTrue(ResumptionTokenHelper.getSet(MANDATORY_PARTS_TOKEN).isEmpty());
        assertTrue(ResumptionTokenHelper.getFormat(MANDATORY_PARTS_TOKEN).isEmpty());
        assertNotNull(ResumptionTokenHelper.getExpirationDate(MANDATORY_PARTS_TOKEN));
        assertEquals(COMPLETE_LIST_SIZE, ResumptionTokenHelper.getCompleteListSize(MANDATORY_PARTS_TOKEN));
        assertEquals(CURSOR, ResumptionTokenHelper.getCursor(MANDATORY_PARTS_TOKEN));
        assertTrue(ResumptionTokenHelper.getCursorMark(MANDATORY_PARTS_TOKEN).isEmpty());
    }

    @Test
    public void testEncodeDecode() {
        Date expirationDate = new Date(System.currentTimeMillis() + TTL);
        ResumptionToken encodedToken = ResumptionTokenHelper.createResumptionToken(FROM, UNTIL, SET, FORMAT, expirationDate, COMPLETE_LIST_SIZE, CURSOR, NEXT_CURSOR_MARK);

        String decodedValue = new String(Base64.getUrlDecoder().decode(encodedToken.getValue()));
        assertTrue(decodedValue.contains(FROM));
        assertTrue(decodedValue.contains(UNTIL));
        assertTrue(decodedValue.contains(SET));
        assertTrue(decodedValue.contains(FORMAT));
        assertTrue(decodedValue.contains(String.valueOf(expirationDate.getTime())));
        assertTrue(decodedValue.contains(String.valueOf(COMPLETE_LIST_SIZE)));
        assertTrue(decodedValue.contains(String.valueOf(CURSOR)));
        assertTrue(decodedValue.contains(NEXT_CURSOR_MARK));

        ResumptionToken decodedToken = ResumptionTokenHelper.decodeResumptionToken(encodedToken.getValue());
        assertEquals(FROM, ResumptionTokenHelper.getFrom(decodedToken.getValue()));
        assertEquals(UNTIL, ResumptionTokenHelper.getUntil(decodedToken.getValue()));
        assertEquals(SET, ResumptionTokenHelper.getSet(decodedToken.getValue()));
        assertEquals(FORMAT, ResumptionTokenHelper.getFormat(decodedToken.getValue()));
        assertEquals(expirationDate, ResumptionTokenHelper.getExpirationDate(decodedToken.getValue()));
        assertEquals(COMPLETE_LIST_SIZE, ResumptionTokenHelper.getCompleteListSize(decodedToken.getValue()));
        assertEquals(CURSOR, ResumptionTokenHelper.getCursor(decodedToken.getValue()));
        assertEquals(NEXT_CURSOR_MARK, ResumptionTokenHelper.getCursorMark(decodedToken.getValue()));
    }
}
