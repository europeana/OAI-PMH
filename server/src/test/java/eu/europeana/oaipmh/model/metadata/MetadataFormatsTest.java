package eu.europeana.oaipmh.model.metadata;

import eu.europeana.oaipmh.model.ListMetadataFormats;
import eu.europeana.oaipmh.model.MetadataFormat;
import eu.europeana.oaipmh.service.exception.NoMetadataFormatsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@SpringBootTest
public class MetadataFormatsTest {
    private static final String METADATA_FORMAT_PREFIX = "format_prefix";

    private static final String METADATA_FORMAT_SCHEMA = "format_schema";

    private static final String METADATA_FORMAT_NAMESPACE = "format_namespace";

    private static final String EU_EUROPEANA_OAIPMH_MODEL_METADATA_XML2_EDMCONVERTER = "eu.europeana.oaipmh.model.metadata.XML2EDMConverter";

    private List<String> prefixes = new ArrayList<>();

    private Map<String, String> converters = new HashMap<>();

    private Map<String, String> schemas = new HashMap<>();

    private Map<String, String> namespaces = new HashMap<>();

    private MetadataFormats testedMetadataFormats = new MetadataFormats();

    @Before
    public void init() throws InvocationTargetException, IllegalAccessException {
        prefixes.add(METADATA_FORMAT_PREFIX);
        Whitebox.setInternalState(testedMetadataFormats, "prefixes", prefixes);
        converters.put(METADATA_FORMAT_PREFIX, EU_EUROPEANA_OAIPMH_MODEL_METADATA_XML2_EDMCONVERTER);
        Whitebox.setInternalState(testedMetadataFormats, "converters", converters);
        schemas.put(METADATA_FORMAT_PREFIX, METADATA_FORMAT_SCHEMA);
        Whitebox.setInternalState(testedMetadataFormats, "schemas", schemas);
        namespaces.put(METADATA_FORMAT_PREFIX, METADATA_FORMAT_NAMESPACE);
        Whitebox.setInternalState(testedMetadataFormats, "namespaces", namespaces);
        Whitebox.getMethod(MetadataFormats.class, "initFormats").invoke(testedMetadataFormats);
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
    public void listMetadataFormats() throws NoMetadataFormatsException {
        ListMetadataFormats formats = createListMetadataFormats();

        ListMetadataFormats retrieved = testedMetadataFormats.listMetadataFormats();
        assertEquals(formats.getMetadataFormats().size(), retrieved.getMetadataFormats().size());
        assertEquals(retrieved.getMetadataFormats().size(), 1);
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