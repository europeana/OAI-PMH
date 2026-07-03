package eu.europeana.oaipmh.model.serialize;

import com.ctc.wstx.sw.BaseStreamWriter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.metis.utils.EdmUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

import javax.xml.stream.XMLStreamWriter;

public class FullBeanSerializer extends JsonSerializer<FullBeanImpl> {

    @Override
    public void serialize(
            FullBeanImpl bean, JsonGenerator jg, SerializerProvider provider) 
            throws IOException {
        Optional<Writer> opt = getWriter(jg);
        if (opt.isEmpty()) {
            String edm = EdmUtils.toEDM(bean);
            if ( edm != null ) { jg.writeRaw(removeXMLHeader(edm)); }
            return;
        }

        //necessary so that the parent element gets closed
        jg.writeRaw("");
        EdmUtils.toEDM(bean, opt.get(), false);
    }

    private Optional<Writer> getWriter(JsonGenerator jg) {
        if (!(jg instanceof ToXmlGenerator)) { return Optional.empty(); }
        
        XMLStreamWriter writer = ((ToXmlGenerator)jg).getStaxWriter();
        if (writer instanceof BaseStreamWriter) {
            return Optional.of(((BaseStreamWriter)writer).wrapAsRawWriter());
        }
        return Optional.empty();
    }

    private String removeXMLHeader(String xml) {
        int index = xml.indexOf("?>");
        return (index > -1 ? xml : xml.substring(index + "?>".length()));
    }
}
