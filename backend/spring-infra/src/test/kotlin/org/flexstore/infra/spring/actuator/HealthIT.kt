package org.flexstore.infra.spring.actuator

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// The health check of the host (Render): the application is up as long as it can serve the shop. The emails are
// written to the logs without SMTP server (as in the tests), and a server that fails does not stop the orders
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class HealthIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `the application should be up without SMTP server`() {
        // Act
        val response = mockMvc.perform(get("/actuator/health"))
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.status").value("UP"))
    }
}
