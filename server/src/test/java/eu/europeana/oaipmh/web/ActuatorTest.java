package eu.europeana.oaipmh.web;

import eu.europeana.oaipmh.AbstractIntegrationIT;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * JUnit test for testing if the /actuator/info endpoint is available
 */
@SpringBootTest
public class ActuatorTest extends AbstractIntegrationIT {

    @Test
    void testActuatorInfo() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }
}