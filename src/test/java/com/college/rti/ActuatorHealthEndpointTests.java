package com.college.rti;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ActuatorHealthEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Verify Actuator /actuator/health returns 200 OK and UP status with JSON response")
    void healthEndpointShouldReturnUp() throws Exception {
        mockMvc.perform(get("/actuator/health")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Verify Actuator /actuator/info endpoint responds successfully with 200 OK")
    void infoEndpointShouldReturnOk() throws Exception {
        mockMvc.perform(get("/actuator/info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
