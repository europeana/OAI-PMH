package eu.europeana.oaipmh;

import eu.europeana.oaipmh.service.*;
import eu.europeana.oaipmh.util.MemoryUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Main application and configuration.
 *
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class})
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
     * Main entry point of this application
     *
     * @param args command-line arguments
     */
    public static void main(String[] args)  {
        LOG.info("Configure Spring Application!!");
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

