package eu.europeana.oaipmh.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.oaipmh.model.metadata.MetadataFormatsService;
import eu.europeana.oaipmh.model.serialize.DefaultSerializationProvider;
import eu.europeana.oaipmh.model.serialize.SerializationHandler;
import eu.europeana.oaipmh.model.serialize.ServerSerializationProvider;
import eu.europeana.oaipmh.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;

import static eu.europeana.oaipmh.util.AppConfigConstants.*;

@Configuration
public class OaiPmhConfig {

    private static final Logger LOG = LogManager.getLogger(OaiPmhConfig.class);

    @Resource
    OaiPmhSettings settings;

    /**
     * Creates and returns an instance of the {@link RecordProvider} based on the fully qualified
     * class name specified in the application settings. If the specified record provider class
     * cannot be instantiated due to various exceptions, a default implementation of
     * {@link RecordProvider}, {@link RecordApi}, is returned.
     *
     * @return an instantiated {@link RecordProvider} based on the class name from the application settings,
     *         or a default implementation {@link RecordApi} in case of errors.
     * @throws RuntimeException if an error occurs while attempting to instantiate the specified class.
     */
    @Bean(RECORD_PROVIDER_BEAN)
    public RecordProvider recordProvider() {
        try {
            Class providerClass = Class.forName(settings.getRecordProviderClass());
            if (providerClass != null) {
                Constructor constructor = providerClass.getConstructor();
                if (constructor != null) {
                    LOG.info("Instantiating record provider: {}", settings.getRecordProviderClass());
                    return (RecordProvider) constructor.newInstance();
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            LOG.error("Problem with instantiating record provider.", e);
            throw new RuntimeException(e);
        }

        // return RecordApi when there are problems with the specified class
        LOG.info("Using default record provider: {}", RecordApi.class.getName());
        return new RecordApi();
    }

    /**
     * Provides a bean for managing the Identifier operation in the OAI-PMH
     * This method returns an implementation of the {@link IdentifierProvider} interface.
     *
     * @return an instance of {@link IdentifierProvider}, specifically the default implementation {@link SearchApi}.
     */
    @Bean
    public IdentifierProvider identifierProvider() {
        LOG.info("Using default identifier provider: {}", SearchApi.class.getName());
        return new SearchApi();
    }


    /**
     * Provides a bean that creates and configures an instance of {@link MetadataFormatsService}.
     * The {@link MetadataFormatsService} is responsible for managing metadata format converters,
     * schemas, namespaces, and related configurations defined in the application settings.
     *
     * @return an instance of {@link MetadataFormatsService}, which is configured to handle metadata
     * formats and their associated converters, schemas, and namespaces.
     */
    @Bean
    public MetadataFormatsService metadataFormats() {
        return new MetadataFormatsService();
    }

    /**
     * Provides a bean for managing the Identify operation in the OAI-PMH
     * This method returns a default implementation of the
     * {@link IdentifyProvider} interface.
     *
     * @return an instance of {@link IdentifyProvider}, specifically the default implementation {@link DefaultIdentifyProvider}.
     */
    @Bean
    public IdentifyProvider identifyProvider() {
        LOG.info("Using default identify provider: {}", DefaultIdentifyProvider.class.getName());
        return new DefaultIdentifyProvider();
    }


    /**
     * Provides a bean for managing sets in the OAI-PMH
     * The method returns an implementation of the {@link SetsProvider} interface.
     *
     * @return an instance of {@link SetsProvider}, specifically the default implementation {@link DefaultSetsProvider}.
     */
    @Bean
    public SetsProvider setsProvider() {
        LOG.info("Using default sets provider: {}", DefaultSetsProvider.class.getName());
        return new DefaultSetsProvider();
    }

    /**
     * Configures and provides a {@link CorsFilter} bean to handle Cross-Origin Resource Sharing (CORS) settings.
     * This filter allows cross-origin requests based on the specified configuration, which permits all headers,
     * origins, and methods, while also exposing certain headers such as "Allow", "Vary", "ETag", and "Last-Modified".
     * It also sets a maximum age for preflight requests.
     *
     * @return an instance of {@link CorsFilter} with the configured CORS settings
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
     * Provides an instance of {@link XmlMapper} for serialization purposes.
     * This method registers a {@link ServerSerializationProvider} with the {@link SerializationHandler},
     * which configures the provider to use an {@link XmlMapper} with custom serialization settings.
     *
     * @return an instance of {@link XmlMapper} configured for serialization.
     */
    @Primary
    @Bean(XML_SERVER_SERIALIZATION)
    public XmlMapper serverSerialization() {
        LOG.info("Registering XmlMapper server serialization provider..... ");
        SerializationHandler.register(new ServerSerializationProvider());
        return SerializationHandler.getSerialization();
    }

    @Bean(XML_DEFAULT_SERIALIZATION)
    public XmlMapper defaultSerialization() {
        LOG.info("Registering XmlMapper default serialization provider..... ");
        SerializationHandler.register(new DefaultSerializationProvider());
        return SerializationHandler.getSerialization();
    }
}
