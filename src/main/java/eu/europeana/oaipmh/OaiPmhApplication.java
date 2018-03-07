package eu.europeana.oaipmh;

import eu.europeana.oaipmh.service.*;
import eu.europeana.oaipmh.web.VerbController;
import eu.europeana.oaipmh.web.context.SocksProxyConfigInjector;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Main application and configuration.
 *
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@SpringBootApplication
@PropertySource("classpath:oai-pmh.properties")
@PropertySource(value = "classpath:oai-pmh.user.properties", ignoreResourceNotFound = true)
public class OaiPmhApplication extends SpringBootServletInitializer {

	/**
	 * Record provider that returns record information
	 * @return
	 */
	@Bean
	public RecordProvider recordProvider() {
		// for now we use the Europeana Record API, but if we include other options we should probably make this configurable
		return new RecordApi();
	}

	/**
	 * Identifiers provider that returns identifiers information
	 * @return
	 */
	@Bean
	public IdentifierProvider identifierProvider() {
		return new SearchApi();
	}

	/**
	 * OAI-PMH service that does the actual work
	 * @return
	 */
	@Bean
	public OaiPmhService oaiPmhService() {
		return new OaiPmhService(recordProvider(), identifierProvider());
	}

	/**
	 * Rest controller that handles all requests
	 * @return
	 */
	@Bean
	public VerbController verbController() {
		return new VerbController(oaiPmhService());
	}

	/**
	 * This method is called when starting as a Spring-Boot application ('run' this class from your IDE)
	 * @param args
	 */
	@SuppressWarnings("squid:S2095") // to avoid sonarqube false positive (see https://stackoverflow.com/a/37073154/741249)
	public static void main(String[] args)  {
		try {
			injectSocksProxySettings();
			SpringApplication.run(OaiPmhApplication.class, args);
		} catch (IOException e) {
			LogManager.getLogger(OaiPmhApplication.class).fatal("Error reading properties file", e);
			System.exit(-1);
		}
	}

	/**
	 * This method is called when starting a 'traditional' war deployment (e.g. in Docker of Cloud Foundry)
	 * @param servletContext
	 * @throws ServletException
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		try {
			injectSocksProxySettings();
			super.onStartup(servletContext);
		} catch (IOException e) {
			throw new ServletException("Error reading properties", e);
		}
	}

	/**
	 * Socks proxy settings have to be loaded before anything else, so we check the property files for its settings
	 * @throws IOException
	 */
	private static void injectSocksProxySettings() throws IOException {
		SocksProxyConfigInjector socksConfig = new SocksProxyConfigInjector("oai-pmh.properties");
		try {
			socksConfig.addProperties("oai-pmh.user.properties");
		} catch (IOException e) {
			// user.properties may not be available so only show warning
			LogManager.getLogger(OaiPmhApplication.class).warn("Cannot read oai-pmh.user.properties file");
		}
		socksConfig.inject();
	}

}
