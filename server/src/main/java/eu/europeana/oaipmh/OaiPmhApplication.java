package eu.europeana.oaipmh;

import eu.europeana.oaipmh.model.metadata.MetadataFormats;
import eu.europeana.oaipmh.service.*;
import eu.europeana.oaipmh.util.SocksProxyHelper;
import eu.europeana.oaipmh.web.VerbController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Main application and configuration.
 *
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@SpringBootApplication
@PropertySource("classpath:oai-pmh.properties")
@PropertySource(value = "classpath:oai-pmh.user.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:build.properties", ignoreResourceNotFound = true)
@EnableConfigurationProperties(MetadataFormats.class)
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class, MongoAutoConfiguration.class})
public class OaiPmhApplication extends SpringBootServletInitializer {

    private static final Logger LOG = LogManager.getLogger(OaiPmhApplication.class);

	@Value("${recordProviderClass}")
	private String recordProviderClass;

	/**
	 * Record provider that returns record information
	 * @return
	 */
	@Bean
	public RecordProvider recordProvider() {
        try {
            Class providerClass = Class.forName(recordProviderClass);
            if (providerClass != null) {
                Constructor constructor = providerClass.getConstructor();
                if (constructor != null) {
                    return (RecordProvider) constructor.newInstance();
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            LOG.error("Problem with instantiating record provider.", e);
            throw new RuntimeException(e);
        }

        // return RecordApi when there are problems with the specified class
		return new RecordApi();
	}

	/**
	 * Identifiers provider that returns identifiers information
	 * @return object implementing IdentifierProvider interface
	 */
	@Bean
	public IdentifierProvider identifierProvider() {
		return new SearchApi();
	}

	/**
	 * Handles metadata formats for identifiers and records.
	 *
	 * @return object implementing MetadataFormats interface
	 */
	@Bean
	public MetadataFormats metadataFormats() { return new MetadataFormats(); }

	/**
	 * Handles providing information for Identify verb.
	 *
	 * @return object implementing IdentifyProvider interface
	 */
	@Bean
	public IdentifyProvider identifyProvider() { return new DefaultIdentifyProvider(); }

	/**
	 * Handles providing information for ListSets verb.
	 *
	 * @return object implementing SetsProvider interface
	 */
	@Bean
	public SetsProvider setsProvider() { return new DefaultSetsProvider(); }

	/**
	 * OAI-PMH service that does the actual work
	 * @return
	 */
	@Bean
	public OaiPmhService oaiPmhService() {
		return new OaiPmhService(recordProvider(), identifierProvider(), identifyProvider(), metadataFormats(), setsProvider());
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
			SocksProxyHelper.injectSocksProxySettings();
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
			SocksProxyHelper.injectSocksProxySettings();
			super.onStartup(servletContext);
		} catch (IOException e) {
			throw new ServletException("Error reading properties", e);
		}
	}

}
