package eu.europeana.oaipmh.util;

import eu.europeana.oaipmh.model.ResumptionToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ResumptionTokenHelperTest {
    private static final String INCORRECT_TOKEN = "AHGVHFGYDUSJKSBDSBHSVDJSVDYUASAHJISAKBB767873213HVHGVGH";

    private static final String NEXT_CURSOR_MARK = "ABC";

    private static final long COMPLETE_LIST_SIZE = 1234L;

    private static final long CURSOR = 300;

    private static final long TTL = 84600L;

    private static final List<String> FILTER = Arrays.asList(new String[] {"a:\"b\"", "x:[y TO z]"});

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeIncorrectToken() {
        ResumptionTokenHelper.decodeResumptionToken(INCORRECT_TOKEN);
    }

    @Test
    public void testEncodeDecode() {
        Date expirationDate = new Date(System.currentTimeMillis() + TTL);
        ResumptionToken encodedToken = ResumptionTokenHelper.createResumptionToken(NEXT_CURSOR_MARK, COMPLETE_LIST_SIZE, expirationDate, CURSOR, FILTER);

        String decodedValue = new String(Base64.getUrlDecoder().decode(encodedToken.getValue()));
        assertTrue(decodedValue.contains(String.valueOf(CURSOR)));
        assertTrue(decodedValue.contains(FILTER.get(0)));
        assertTrue(decodedValue.contains(FILTER.get(1)));
        assertTrue(decodedValue.contains(NEXT_CURSOR_MARK));
        assertTrue(decodedValue.contains(String.valueOf(expirationDate.getTime())));

        ResumptionToken decodedToken = ResumptionTokenHelper.decodeResumptionToken(encodedToken.getValue());
        assertEquals(encodedToken.getExpirationDate(), decodedToken.getExpirationDate());
        assertEquals(encodedToken.getFilterQuery(), decodedToken.getFilterQuery());
        assertEquals(encodedToken.getCursor(), decodedToken.getCursor());
        assertEquals(NEXT_CURSOR_MARK, decodedToken.getValue());
    }
}
