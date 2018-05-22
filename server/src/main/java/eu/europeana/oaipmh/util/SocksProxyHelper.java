package eu.europeana.oaipmh.util;

import eu.europeana.oaipmh.web.context.SocksProxyConfigInjector;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class SocksProxyHelper {

    private SocksProxyHelper() {}

    /**
     * Socks proxy settings have to be loaded before anything else, so we check the property files for its settings
     * @throws IOException
     */
    public static void injectSocksProxySettings() throws IOException {
        SocksProxyConfigInjector socksConfig = new SocksProxyConfigInjector("oai-pmh.properties");
        try {
            socksConfig.addProperties("oai-pmh.user.properties");
        } catch (IOException e) {
            // user.properties may not be available so only show warning
            LogManager.getLogger(SocksProxyHelper.class).warn("Cannot read oai-pmh.user.properties file");
        }
        socksConfig.inject();
    }
}
