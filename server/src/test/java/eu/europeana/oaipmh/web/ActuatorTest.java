package eu.europeana.oaipmh.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    public void testActuatorInfo() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().is(HttpStatus.OK.value()));
    }

}
