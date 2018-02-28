package eu.europeana.oaipmh.web.context;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

/**
 * Reads socks proxy configuration (if available) from the provided properties file and adds this as system properties.
 * Note that for connections to use the socks proxy, the injection needs to be done early during startup before any
 * connections are setup. Also note that Java NIO classes do not support socks proxy.
 *
 * @author Patrick Ehlert on 13-1-18.
 */
public class SocksProxyConfigInjector {

    public static final String SOCKS_ENABLED = "socks.enabled";
    public static final String SOCKS_HOST = "socks.host";
    public static final String SOCKS_PORT = "socks.port";
    public static final String SOCKS_USER = "socks.user";
    public static final String SOCKS_PASS = "socks.password";

    private static final Logger LOG = LogManager.getLogger(SocksProxyConfigInjector.class);

    private Properties props = new Properties();

    /**
     * Load socks configuration from a file with the provided file name (file should be on the classpath)
     *
     * @param propertiesFileName
     * @throws IOException thrown if the provided file is not found or cannot be read
     */
    public SocksProxyConfigInjector(String propertiesFileName) throws IOException {
        addProperties(propertiesFileName);
    }

    /**
     * Load socks configuration from a Properties object
     *
     * @param properties
     */
    public SocksProxyConfigInjector(Properties properties) {
        addProperties(properties);
    }

    /**
     * Add additional properties from a file with the provided file name (file should be on the classpath)
     *
     * @param propertiesFileName
     * @return true if the properties were read from file, otherwise false
     */
    public final void addProperties(String propertiesFileName) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFileName)) {
            if (in == null) {
                throw new IOException("Properties file " + propertiesFileName + " doesn't exist!");
            } else {
                props.load(in);
            }
        }
    }

    /**
     * Add additional properties from a Properties object
     *
     * @param properties
     */
    public final void addProperties(Properties properties) {
        props.putAll(properties);
    }

    /**
     * Check if the provided configuration contains a socks proxy and it is set to enabled
     *
     * @return true if there is at least a socks host defined and the socks enabled setting is true
     */
    public boolean isValidConfiguration() {
        String host = props.getProperty(SOCKS_HOST);
        Boolean enabled = Boolean.valueOf(props.getProperty(SOCKS_ENABLED));

        boolean result = false;
        if (StringUtils.isEmpty(host)) {
            LOG.info("No socks proxy configured");
        } else if (enabled == null || !enabled) {
            LOG.info("Socks proxy disabled");
        } else {
            LOG.info("Setting up socks proxy at {}", host);
            result = true;
        }
        return result;
    }

    /**
     * Add the socks proxy settings to the system properties. Note that the isValidConfiguration check is done first, so
     * the settings will only be added if that check passes.
     */
    public void inject() {
        if (isValidConfiguration()) {
            System.setProperty("socksProxyHost", props.getProperty(SOCKS_HOST));
            System.setProperty("socksProxyPort", props.getProperty(SOCKS_PORT));

            String user = props.getProperty(SOCKS_USER);
            if (StringUtils.isNotEmpty(user)) {
                String pass = props.getProperty(SOCKS_PASS);
                System.setProperty("java.net.socks.username", user);
                System.setProperty("java.net.socks.password", pass);
                Authenticator.setDefault(new SockProxyAuthenticator(user, pass));
            }
        }
    }

    private static class SockProxyAuthenticator extends Authenticator {

        private String user;
        private char[] password;

        public SockProxyAuthenticator(String user, String password) {
            this.user = user;
            this.password = password.toCharArray();
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password);
        }
    }

}
