package eu.europeana.oaipmh;

import eu.europeana.oaipmh.service.*;
import eu.europeana.oaipmh.util.MemoryUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

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

