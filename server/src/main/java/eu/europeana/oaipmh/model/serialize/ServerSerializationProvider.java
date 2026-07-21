package eu.europeana.oaipmh.model.serialize;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;

public class ServerSerializationProvider extends DefaultSerializationProvider {

    protected JacksonXmlModule createJacksonModule() {
        JacksonXmlModule module = super.createJacksonModule();
        module.addSerializer(FullBeanImpl.class, new FullBeanSerializer());
        return module;
    }
}
