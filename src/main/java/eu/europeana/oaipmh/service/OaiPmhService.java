package eu.europeana.oaipmh.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import eu.europeana.oaipmh.model.GetRecord;
import eu.europeana.oaipmh.model.Identify;
import eu.europeana.oaipmh.model.OAIResponse;
import eu.europeana.oaipmh.service.exception.OaiPmhException;
import eu.europeana.oaipmh.service.exception.SerializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 *
 *
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@Service
public class OaiPmhService {

    private static final Logger LOG = LogManager.getLogger(OaiPmhService.class);

    // create a single XmlMapper for efficiency purposes (see https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance)
    private static final XmlMapper xmlMapper = new XmlMapper();

    @Value("${recordsPerPage}")
    private int recordsPerPage;

    @Value("${identifiersPerPage}")
    private int identifiersPerPage;

    @Value("${resumptionTokenTTL}")
    private int resumptionTokenTTL;

    private RecordProvider recordProvider;

    public OaiPmhService(RecordProvider recordProvider) {
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // not serialize fields with null value
        xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); // serialize also private fields
        // make sure dates are serialized in proper format
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        xmlMapper.setDateFormat(new ISO8601DateFormat()); // we set this to abbreviate the timezone (not sure how to use non-deprecated method for this)
        xmlMapper.registerModule(new JaxbAnnotationModule()); // so we can use JAX-B annotations instead of the Jackson ones

        this.recordProvider = recordProvider;
    }

    @PostConstruct
    private void init() {
        // Note that properties aren't available until after the construction of the bean
        LOG.info("Records per page: {}", recordsPerPage);
        LOG.info("Identifiers per page: {}", identifiersPerPage);
        LOG.info("Resumption token TTL: {}", resumptionTokenTTL);
    }

    protected XmlMapper getXmlMapper() {
        return xmlMapper;
    }

    /**
     * Return repository information according to OAI-PMH-protocol (see https://www.openarchives.org/OAI/openarchivesprotocol.html#Identify)
     * @return
     * @throws OaiPmhException
     */
    public String getIdentify() throws OaiPmhException {
        return serialize("Identify", new Identify());
    }

    /**
     * Retrieve record information according to OAI-PMH protocol (see https://www.openarchives.org/OAI/openarchivesprotocol.html#GetRecord)
     * @param metadataPrefix
     * @param id
     * @return record information in OAI-PMH (xml)
     * @throws OaiPmhException
     */
    public String getRecord(String metadataPrefix, String id) throws OaiPmhException {
        // TODO check metadataprefix?
        return serialize("GetRecord", new GetRecord(recordProvider.getRecord(id)));
    }

    private String serialize(String verb, Object object) throws SerializationException {
        try {
            // TODO get base url from somewhere
            String baseUrl = "https://oai.europeana.eu/oai";
            return xmlMapper.
                    writerWithDefaultPrettyPrinter().
                    writeValueAsString(new OAIResponse(verb, baseUrl, object));
        }
        catch (IOException e) {
            throw new SerializationException("Error serializing data: "+e.getMessage(), e);
        }
    }



}
