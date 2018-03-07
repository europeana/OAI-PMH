package eu.europeana.oaipmh.service;

import org.springframework.beans.factory.annotation.Value;

public class CommonApi implements ClosableProvider {
    @Value("${wskey}")
    private String wskey;

    protected String appendWskey() {
        return String.format("wskey=%s", wskey);
    }

    /**
     * @see RecordProvider#close()
     */
    public void close() {
        // not needed in this case
    }
}
