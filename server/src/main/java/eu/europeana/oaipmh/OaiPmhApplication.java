package eu.europeana.oaipmh;

import eu.europeana.oaipmh.model.metadata.MetadataFormatsService;
import eu.europeana.oaipmh.service.*;
import eu.europeana.oaipmh.util.SocksProxyHelper;
import eu.europeana.oaipmh.util.SwaggerProvider;
import eu.europeana.oaipmh.web.VerbController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, MongoAutoConfiguration.class})
@PropertySource("classpath:oai-pmh.properties")
@PropertySource(value = "classpath:oai-pmh.user.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:build.properties", ignoreResourceNotFound = true)
public class OaiPmhApplication extends SpringBootServletInitializer {

    private static final Logger LOG = LogManager.getLogger(OaiPmhApplication.class);

    @Value("${recordProviderClass}")
    private String recordProviderClass;

    /**
     * Setup CORS for all requests
     * @return
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*").maxAge(1000)
                        .exposedHeaders("Allow, Vary, ETag, Last-Modified");
            }
        };
    }

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
    public MetadataFormatsService metadataFormats() { return new MetadataFormatsService(); }

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
        return new VerbController(oaiPmhService(), swaggerProvider());
    }


    @Bean
    public SwaggerProvider swaggerProvider() {
        return new SwaggerProvider();
    }

    /**
     * This method is called when starting as a Spring-Boot application (e.g. when running this class from your IDE, or
     * when using Cloud Foundry Java Main i.c.m. the Tomcat embedded by Spring-Boot)
     * @param args
     */
    @SuppressWarnings("squid:S2095") // to avoid sonarqube false positive (see https://stackoverflow.com/a/37073154/741249)
    public static void main(String[] args)  {
        LOG.info("Main start - CF_INSTANCE_GUID = {}, CF_INSTANCE_IP  = {}", System.getenv("CF_INSTANCE_GUID"), System.getenv("CF_INSTANCE_IP"));
        try {
            SocksProxyHelper.injectSocksProxySettings();
            SpringApplication.run(OaiPmhApplication.class, args);
        } catch (IOException e) {
            LogManager.getLogger(OaiPmhApplication.class).fatal("Error reading properties file", e);
            System.exit(-1);
        }
    }

    /**
     * This method is called when starting a 'traditional' Servlet war deployment
     * @param servletContext
     * @throws ServletException
     */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        LOG.info("CF_INSTANCE_GUID = {}, CF_INSTANCE_IP  = {}", System.getenv("CF_INSTANCE_GUID"), System.getenv("CF_INSTANCE_IP"));
        try {
            SocksProxyHelper.injectSocksProxySettings();
            super.onStartup(servletContext);
        } catch (IOException e) {
            throw new ServletException("Error reading properties", e);
        }
    }

}
