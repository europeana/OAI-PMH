package eu.europeana.oaipmh;

import eu.europeana.oaipmh.model.metadata.MetadataFormatsService;
import eu.europeana.oaipmh.service.*;
import eu.europeana.oaipmh.util.MemoryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Main application and configuration.
 *
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, MongoAutoConfiguration.class, EmbeddedMongoAutoConfiguration.class})
@PropertySource("classpath:oai-pmh.properties")
@PropertySource(value = "classpath:oai-pmh.user.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:build.properties", ignoreResourceNotFound = true)
@EnableScheduling
public class OaiPmhApplication extends SpringBootServletInitializer  {

    private static final Logger LOG = LogManager.getLogger(OaiPmhApplication.class);

    @Value("${recordProviderClass}")
    private String recordProviderClass;

    @Scheduled(fixedRate = 300_000) // 5 minutes
    public void logMemoryUsage() {
        // Temporary code to check memory usage / leaks
        System.gc();
        LOG.debug("JVM MEMORY INFO: Used {} ({}), Free {}, Max {}, System free {}",
                MemoryUtils.getTotalMemoryJVMInMB(),
                MemoryUtils.getPercentageUsedFormatted(),
                MemoryUtils.getFreeMemoryJVMInMB(),
                MemoryUtils.getMaxMemoryJVMInMB(),
                MemoryUtils.getFreeMemorySystemInMB());
    }

    /**
     * Setup CORS for all requests
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowedOrigins(Collections.singletonList("*"));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setExposedHeaders(Arrays.asList("Allow", "Vary", "ETag", "Last-Modified"));
        config.setMaxAge(1000L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
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
     * This method is called when starting as a Spring-Boot application (e.g. when running this class from your IDE, or
     * when using Cloud Foundry Java Main i.c.m. the Tomcat embedded by Spring-Boot)
     * @param args
     */
    @SuppressWarnings("squid:S2095") // to avoid sonarqube false positive (see https://stackoverflow.com/a/37073154/741249)
    public static void main(String[] args)  {
        SpringApplication.run(OaiPmhApplication.class, args);

    }

    /**
     * This method is called when starting a 'traditional' Servlet war deployment
     * @param servletContext
     * @throws ServletException
     */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);
    }
}

