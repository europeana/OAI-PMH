package eu.europeana.oaipmh.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for writing results to a logfile each time we process a dataset
 * Note that anything that is sent to the OUT is also visible in the console
 * @author Patrick Ehlert
 * Created on 22-08-2018
 */
public class LogFile {

    private LogFile(){
        throw new IllegalStateException("Utility class");
    }

    static final Logger OUT = LogManager.getLogger("logFile");

    public static void setFileName(String path) {
        // note that this will only work on Linux systems
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
        String logFileName = fileName + "_" + dtf.format(LocalDateTime.now());
        System.setProperty("logFileName", logFileName);

        OUT.debug("Creating new logfile {} ", logFileName);
        org.apache.logging.log4j.core.LoggerContext ctx = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        ctx.reconfigure();
    }
}
