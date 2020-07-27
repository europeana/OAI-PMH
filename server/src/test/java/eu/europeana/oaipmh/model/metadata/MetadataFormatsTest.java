package eu.europeana.oaipmh.model.metadata;

import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.MetadataFormat;
import eu.europeana.oaipmh.model.MetadataFormatConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MetadataFormatsTest {
    private static final String METADATA_FORMAT_PREFIX = "format_prefix";

    private static final String METADATA_FORMAT_SCHEMA = "format_schema";

    private static final String METADATA_FORMAT_NAMESPACE = "format_namespace";

    private static final String EU_EUROPEANA_OAIPMH_MODEL_METADATA_XML2_EDMCONVERTER = "eu.europeana.oaipmh.model.metadata.XML2EDMConverter";

    private List<String> prefixes = new ArrayList<>();

    private Map<String, String> converters = new HashMap<>();

    private Map<String, String> schemas = new HashMap<>();

    private Map<String, String> namespaces = new HashMap<>();

    private Map<String, MetadataFormat> metadataFormats = new HashMap<>();

    private static MetadataFormatsService testedMetadataFormats = new MetadataFormatsService();

    private static MetadataFormat metadataFormat;

    private static MetadataFormatConverter metadataFormatConverter ;

    @Before
    public void init() {

           prefixes.add(METADATA_FORMAT_PREFIX);
           schemas.put(METADATA_FORMAT_PREFIX, METADATA_FORMAT_SCHEMA);
           namespaces.put(METADATA_FORMAT_PREFIX, METADATA_FORMAT_NAMESPACE);
           converters.put(METADATA_FORMAT_PREFIX, EU_EUROPEANA_OAIPMH_MODEL_METADATA_XML2_EDMCONVERTER);
           metadataFormatConverter= new XML2EDMConverter();
           metadataFormat = new MetadataFormat(METADATA_FORMAT_PREFIX, schemas.get(METADATA_FORMAT_PREFIX), namespaces.get(METADATA_FORMAT_PREFIX), metadataFormatConverter);
           metadataFormats.put(METADATA_FORMAT_PREFIX,metadataFormat);

           Whitebox.setInternalState(testedMetadataFormats, "prefixes", prefixes);
           Whitebox.setInternalState(testedMetadataFormats, "converters", converters);
           Whitebox.setInternalState(testedMetadataFormats, "schemas", schemas);
           Whitebox.setInternalState(testedMetadataFormats, "namespaces", namespaces);
           Whitebox.setInternalState(testedMetadataFormats, "metadataFormats", metadataFormats);
    }

    @Test
    public void getConverters() {
        assertFalse(testedMetadataFormats.getConverters().isEmpty());
    }

    @Test
    public void getPrefixes() {
        assertFalse(testedMetadataFormats.getPrefixes().isEmpty());
    }

    @Test
    public void getSchemas() {
        assertFalse(testedMetadataFormats.getSchemas().isEmpty());
    }

    @Test
    public void getNamespaces() {
        assertFalse(testedMetadataFormats.getNamespaces().isEmpty());
    }

    @Test
    public void canDisseminate() {
        assertTrue(testedMetadataFormats.canDisseminate(METADATA_FORMAT_PREFIX));
        assertFalse(testedMetadataFormats.canDisseminate("ANY"));
    }

    @Test
    public void getConverter() {
        assertNotNull(testedMetadataFormats.getConverter(METADATA_FORMAT_PREFIX));
    }

    @Test
    public void listMetadataFormats() {
        ListMetadataFormats formats = createListMetadataFormats();
        ListMetadataFormats retrieved = testedMetadataFormats.listMetadataFormats();

        assertEquals(formats.getMetadataFormats().size(), retrieved.getMetadataFormats().size());
        assertEquals(1, retrieved.getMetadataFormats().size());
        assertEquals(formats.getMetadataFormats().get(0).getMetadataPrefix(), retrieved.getMetadataFormats().get(0).getMetadataPrefix());
        assertEquals(formats.getMetadataFormats().get(0).getSchema(), retrieved.getMetadataFormats().get(0).getSchema());
        assertEquals(formats.getMetadataFormats().get(0).getMetadataNamespace(), retrieved.getMetadataFormats().get(0).getMetadataNamespace());
        assertEquals(formats.getMetadataFormats().get(0).getConverter().getClass(), retrieved.getMetadataFormats().get(0).getConverter().getClass());
    }

    private ListMetadataFormats createListMetadataFormats() {
        ListMetadataFormats formats = new ListMetadataFormats();
        List<MetadataFormat> metadataFormats = new ArrayList<>();
        MetadataFormat format = new MetadataFormat();

        format.setConverter(new XML2EDMConverter());
        format.setSchema(METADATA_FORMAT_SCHEMA);
        format.setMetadataPrefix(METADATA_FORMAT_PREFIX);
        format.setMetadataNamespace(METADATA_FORMAT_NAMESPACE);

        metadataFormats.add(format);
        formats.setMetadataFormats(metadataFormats);
        return formats;
    }
}