package eu.europeana.oaipmh.web;

import eu.europeana.oaipmh.service.IdentifierProvider;
import eu.europeana.oaipmh.service.IdentifyProvider;
import eu.europeana.oaipmh.service.RecordProvider;
import eu.europeana.oaipmh.service.SetsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JUnit test for testing if the /actuator/info endpoint is available
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ActuatorTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock beans to prevent the need to setup connection to Mongo and Solr
    @MockBean
    private RecordProvider recordProvider;
    @MockBean
    private IdentifierProvider identifierProvider;
    @MockBean
    private IdentifyProvider identifyProvider;
    @MockBean
    private SetsProvider setsProvider;

    @Test
    public void testActuatorInfo() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

}
