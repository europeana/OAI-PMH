package eu.europeana.metis.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;
import org.springframework.util.StreamUtils;

import eu.europeana.corelib.edm.utils.EuropeanaUTF8Escaper;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.metis.schema.jibx.RDF;

import org.apache.commons.io.output.CloseShieldWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EdmUtils {
    
    private static final Logger LOG = LogManager.getLogger(EdmUtils.class);
    
    private static IBindingFactory bfact;
    
    static {
        try {
            bfact = BindingDirectory.getFactory(RDF.class);
        } catch (JiBXException e) {
            e.printStackTrace();
        }
    }

    /*
    REMOVE synchronized
    public static synchronized RDF toRDF(FullBeanImpl fullBean);
    public static synchronized RDF toRDF(FullBeanImpl fullBean, boolean preserveIdentifiers);


    */

    @Deprecated
    public static String toEDM(FullBeanImpl fullBean) throws IOException {
        try (StringWriter out = new StringWriter()) {
            toEDM(fullBean, out, true);
            return out.toString();
        }
    }

    public static void toEDM(
            FullBeanImpl fullBean, Writer writer, boolean docHeader) 
            throws IOException {
        RDF rdf = eu.europeana.corelib.edm.utils.EdmUtils.toRDF(fullBean);
        toEDM(rdf, writer, docHeader);
    }

    public static void toEDM(
            FullBeanImpl fullBean, OutputStream out, boolean docHeader) 
            throws IOException {
        RDF rdf = eu.europeana.corelib.edm.utils.EdmUtils.toRDF(fullBean);
        toEDM(rdf, out, docHeader);
    }


    public static void toEDM(RDF rdf, OutputStream out, boolean docHeader) 
            throws IOException {
        IMarshallingContext ctxt;
        try {
            ctxt = bfact.createMarshallingContext();
            // need to shield close() method
            ctxt.setOutput(StreamUtils.nonClosing(out)
                         , "UTF-8", EuropeanaUTF8Escaper.s_instance);
            if ( docHeader ) { ctxt.marshalDocument(rdf, "UTF-8", true); }
            else { ctxt.marshalDocument(rdf); }
        }
        catch (JiBXException e) {
            LOG.error("Error marshalling RDF of record {}", getRecordId(rdf), e);
            throw new IOException("Error marshalling RDF of record", e);
        }
    }

    public static void toEDM(RDF rdf, OutputStream out) throws IOException {
        toEDM(rdf, out, true);
    }

    public static void toEDM(RDF rdf, Writer writer, boolean docHeader) 
            throws IOException {
        IMarshallingContext ctxt;
        try {
            ctxt = bfact.createMarshallingContext();
            // need to shield close() method
            ctxt.setOutput(CloseShieldWriter.wrap(writer)
                         , EuropeanaUTF8Escaper.s_instance);
            if ( docHeader ) { ctxt.marshalDocument(rdf, "UTF-8", true); }
            else { ctxt.marshalDocument(rdf); }
        }
        catch (JiBXException e) {
            LOG.error("Error marshalling RDF of record {}", getRecordId(rdf), e);
            throw new IOException("Error marshalling RDF of record", e);
        }
    }

    public static void toEDM(RDF rdf, Writer writer) throws IOException {
        toEDM(rdf, writer, true);
    }

    @Deprecated
    public static String toEDM(RDF rdf, boolean docHeader) 
            throws IOException {
        try (StringWriter out = new StringWriter()) {
            toEDM(rdf, out, docHeader);
            return out.toString();
        }
    }

    @Deprecated
    public static String toEDM(RDF rdf) throws IOException {
        return toEDM(rdf, true);
    }

    private static String getRecordId(RDF rdf) {
        if (rdf != null && rdf.getProvidedCHOList() != null && !rdf.getProvidedCHOList().isEmpty()) {
            return rdf.getProvidedCHOList().get(0).getAbout();
        }
        return "?";
    }
}
