package eu.europeana.oaipmh.model.metadata;

import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.MetadataFormat;
import eu.europeana.oaipmh.model.MetadataFormatConverter;
import eu.europeana.oaipmh.service.exception.NoMetadataFormatsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Configuration class keeping the metadata converters defined in the configuration file. For each entry from the file
 * the converter of the specified class is created.
 */
@ConfigurationProperties(prefix="metadata.formats")
public class MetadataFormatsService implements MetadataFormatsProvider {

    @Value("#{'${metadata.formats.prefixes}'.split(',')}")
    private final List<String> prefixes = new ArrayList<>();

    private final Map<String, String> converters = new HashMap<>();

    private final Map<String, String> schemas = new HashMap<>();

    private final Map<String, String> namespaces = new HashMap<>();

    private Map<String, MetadataFormat> metadataFormats = new HashMap<>();

    public Map<String, String> getConverters() {
        return converters;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public Map<String, String> getSchemas() {
        return schemas;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    @PostConstruct
    private void initFormats() {
        for (String prefix : prefixes) {
            if (schemas.get(prefix) != null && namespaces.get(prefix) != null) {
                MetadataFormat format = new MetadataFormat(prefix, schemas.get(prefix), namespaces.get(prefix), createConverter(converters.get(prefix)));
                metadataFormats.put(prefix, format);
            }
        }
    }

    private MetadataFormatConverter createConverter(String className) {
        try {
            Class converterClass = Class.forName(className);

            return (MetadataFormatConverter) converterClass.getConstructor(new Class[] {}).newInstance(new Object[] {});
        } catch (ClassNotFoundException |
                NoSuchMethodException |
                IllegalAccessException |
                InstantiationException |
                InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public boolean canDisseminate(String metadataFormat) {
        MetadataFormat format = metadataFormats.get(metadataFormat);
        return format != null && format.getConverter() != null;
    }

    @Override
    public MetadataFormatConverter getConverter(String metadataFormat) {
        if (metadataFormats.get(metadataFormat) == null) {
            return null;
        }
        return metadataFormats.get(metadataFormat).getConverter();
    }

    @Override
    public ListMetadataFormats listMetadataFormats() throws NoMetadataFormatsException {
        if (metadataFormats.isEmpty()) {
            throw new NoMetadataFormatsException("There are no metadata formats available.");
        }
        ListMetadataFormats result = new ListMetadataFormats();
        result.setMetadataFormats(new ArrayList<>(metadataFormats.values()));
        return result;
    }
}
