package eu.europeana.oaipmh.model.metadata;

import eu.europeana.oaipmh.AbstractIntegrationIT;
import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.MetadataFormat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MetadataFormatsTest extends AbstractIntegrationIT {

    @Test
    void shouldLoadMetadataFormatFromProperties() {
        assertNotNull(metadataFormatsService);

        assertNotNull(metadataFormatsService.getPrefixes());
        assertTrue(metadataFormatsService.getPrefixes().contains(METADATA_FORMAT_PREFIX));

        assertNotNull(metadataFormatsService.getSchemas());
        assertTrue(metadataFormatsService.getSchemas().get(METADATA_FORMAT_PREFIX).equals(METADATA_FORMAT_SCHEMA));

        assertNotNull(metadataFormatsService.getNamespaces());
        assertTrue(metadataFormatsService.getNamespaces().get(METADATA_FORMAT_PREFIX).equals(METADATA_FORMAT_NAMESPACE));

        assertNotNull(metadataFormatsService.getConverter(METADATA_FORMAT_PREFIX));
        assertTrue(metadataFormatsService.getConverter(METADATA_FORMAT_PREFIX).getClass().getName().equals(EU_EUROPEANA_OAIPMH_MODEL_METADATA_XML2_EDMCONVERTER));
    }

    @Test
    void shouldCreateMetadataFormats() {
        var formats = metadataFormatsService.listMetadataFormats();
        assertNotNull(formats);
    }

    @Test
    void shouldReturnConverterWhenFormatExists() {
        assertTrue(metadataFormatsService.canDisseminate(METADATA_FORMAT_PREFIX));
    }

    @Test
    void shouldReturnNullForUnknownFormat() {
        assertFalse(metadataFormatsService.canDisseminate("unknown"));
        assertNull(metadataFormatsService.getConverter("unknown"));
    }

    @Test
    public void listMetadataFormats() {
        ListMetadataFormats retrieved = metadataFormatsService.listMetadataFormats();

        assertEquals(1, retrieved.getMetadataFormats().size());
        MetadataFormat format = retrieved.getMetadataFormats().stream().findFirst().get();
        assertEquals(METADATA_FORMAT_PREFIX, format.getMetadataPrefix());
        assertEquals(METADATA_FORMAT_SCHEMA, format.getSchema());
        assertEquals(METADATA_FORMAT_NAMESPACE, format.getMetadataNamespace());
        assertEquals(EU_EUROPEANA_OAIPMH_MODEL_METADATA_XML2_EDMCONVERTER, format.getConverter().getClass().getName());
    }
}