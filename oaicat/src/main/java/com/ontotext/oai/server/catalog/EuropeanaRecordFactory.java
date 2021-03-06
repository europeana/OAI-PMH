package com.ontotext.oai.server.catalog;

import ORG.oclc.oai.server.catalog.RecordFactory;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.util.DateConverter;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Simo
 * Date: 13-12-9
 * Time: 11:29
 */

public class EuropeanaRecordFactory extends RecordFactory {
//    private static Log logger = LogFactory.getLog(EuropeanaRecordFactory.class);
    private final String baseUrl;

    public EuropeanaRecordFactory(Properties properties) {
        super(properties);
        baseUrl = properties.getProperty("EuropeanaRecordFactory.baseUrl", "http://data.europeana.eu/item");

    }

    @Override
    public String fromOAIIdentifier(String identifier) {
        if (identifier.length() < baseUrl.length()) {
            return ""; // not sure what to return but empty string looks fine
        }

        // not sure if it's a problem to recognize records with different prefix.
        return identifier.substring(baseUrl.length());
    }

    @Override
    public String quickCreate(Object nativeItem, String schemaURL, String metadataPrefix) throws IllegalArgumentException, CannotDisseminateFormatException {
        return null; // not supported
    }

    @Override
    public String getOAIIdentifier(Object nativeItem) {
        String localIdentifier = asRecord(nativeItem).eid;
        return baseUrl + localIdentifier;
    }

    @Override
    public String getDatestamp(Object nativeItem) {
        Date timeStamp = asRecord(nativeItem).last_checked;
        if (timeStamp != null) {
            return DateConverter.toIsoDate(timeStamp);
        }
        return null;
    }

    @Override
    public Iterator getSetSpecs(Object nativeItem) throws IllegalArgumentException {
        String setSpec = asRecord(nativeItem).cid;
        if (setSpec != null) {
            return Arrays.asList(new String[] {setSpec}).iterator();
        }
        return null;
    }

    @Override
    public boolean isDeleted(Object nativeItem) {
        return asRecord(nativeItem).deleted;
    }

    @Override
    public Iterator getAbouts(Object nativeItem) {
        return null;
    }

    private static RegistryInfo asRecord(Object nativeItem) throws IllegalArgumentException {
        if (nativeItem instanceof RegistryInfo) {
            return (RegistryInfo) nativeItem;
        }

        throw new IllegalArgumentException();
    }
}
