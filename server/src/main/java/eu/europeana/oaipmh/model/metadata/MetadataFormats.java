package eu.europeana.oaipmh.model.metadata;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class keeping the metadata converters defined in the configuration file. For each entry from the file
 * the converter of the specified class is created.
 */
@ConfigurationProperties(prefix="metadata.formats")
public class MetadataFormats implements MetadataFormatProvider {

    private final Map<String, String> converters = new HashMap<>();

    private Map<String, MetadataFormatConverter> metadataConverters = new HashMap<>();

    public Map<String, String> getConverters() {
        return converters;
    }

    @PostConstruct
    private void initConverters() {
        for (Map.Entry<String, String> entry : converters.entrySet()) {
            MetadataFormatConverter converter = createConverter(entry.getValue());
            if (converter != null) {
                metadataConverters.put(entry.getKey(), converter);
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
        return converters.get(metadataFormat) != null;
    }

    @Override
    public MetadataFormatConverter getConverter(String metadataFormat) {
        if (metadataConverters.get(metadataFormat) == null) {
            return null;
        }
        return metadataConverters.get(metadataFormat);
    }
}
