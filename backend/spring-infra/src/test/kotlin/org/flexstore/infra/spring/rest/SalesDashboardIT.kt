package org.flexstore.infra.spring.rest

import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SalesDashboardIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `an administrator should read the sales of the last 30 days by default`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/sales-dashboard").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.days.length()").value(30))
            .andExpect(jsonPath("$.currency").value("€"))
            .andExpect(jsonPath("$.bestSellers").isArray)
    }

    @Test
    fun `an administrator should choose 7 days`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/sales-dashboard").param("days", "7").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.days.length()").value(7))
    }

    @Test
    fun `another number of days should be refused`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/sales-dashboard").param("days", "10").with(asAdmin()))
        // Assert
        response.andExpect(status().isBadRequest)
    }

    @Test
    fun `a customer should not read the sales`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/sales-dashboard").with(asUser("it-sales-customer")))
        // Assert
        response.andExpect(status().isForbidden)
    }
}
