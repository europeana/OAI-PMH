package eu.europeana.oaipmh.client;

import eu.europeana.oaipmh.model.Record;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtility {

    private static final Logger LOG = LogManager.getLogger(ZipUtility.class);


    private ZipUtility() {
        //adding a private constructor to hide implicit public one
    }

    public static void writeInZip(ZipOutputStream zout, OutputStreamWriter writer, Record record) {
        try {
            zout.putNextEntry(new ZipEntry(getEntryName(record)));
            writer.write(record.getMetadata().getMetadata());
            writer.flush();
            zout.closeEntry();
        } catch (IOException e) {
            LOG.error("Error writing the zip entry", e);
        }

    }

    private static String getEntryName(Record record) {
        String id = record.getHeader().getIdentifier();
        return StringUtils.substringAfterLast(id,Constants.PATH_SEPERATOR) + Constants.EXTENSION ;
    }

    public static String getDirectoryName( String identifier) {
        String id = StringUtils.remove(identifier, Constants.BASE_URL);
        return id.split(Constants.PATH_SEPERATOR)[0];
    }
}
