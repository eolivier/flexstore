package org.flexstore.infra.spring.rest

import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// Product 5 of the catalog (10" Tablet): its threshold is given by the tests, its stock refilled to 100 units
private const val PRODUCT_ID = "5"

@ExtendWith(RefilledStocks::class)
@ExtendWith(ExistingCustomer::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class StockAlertIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private var thresholdBefore = 0

    @BeforeEach
    fun keepThreshold() {
        val readThreshold = "SELECT alert_threshold FROM stock WHERE product_id = ?"
        thresholdBefore = jdbcTemplate.queryForObject(readThreshold, Int::class.java, PRODUCT_ID)!!
    }

    @AfterEach
    fun restoreThreshold() {
        jdbcTemplate.update("UPDATE stock SET alert_threshold = ? WHERE product_id = ?", thresholdBefore, PRODUCT_ID)
    }

    private fun changeThreshold(threshold: Int) = mockMvc.perform(
        put("/api/admin/stocks/$PRODUCT_ID/alert").with(asAdmin())
            .contentType(MediaType.APPLICATION_JSON).content("""{"threshold": $threshold}""")
    )

    @Test
    fun `an admin should give the alert threshold of a product`() {
        // Act
        val response = changeThreshold(12)
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.threshold").value(12))
        mockMvc.perform(get("/api/admin/stocks/$PRODUCT_ID/alert").with(asAdmin()))
            .andExpect(jsonPath("$.threshold").value(12))
    }

    @Test
    fun `a stock at or under the threshold of its product should be listed as low`() {
        // Arrange
        changeThreshold(100)
        // Act
        val response = mockMvc.perform(get("/api/admin/stocks/low").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.productId == '$PRODUCT_ID')].quantity").value(hasItem(100)))
            .andExpect(jsonPath("$[?(@.productId == '$PRODUCT_ID')].threshold").value(hasItem(100)))
    }

    @Test
    fun `a negative alert threshold should be refused`() {
        // Act
        val response = changeThreshold(-1)
        // Assert
        response.andExpect(status().isBadRequest)
    }

    @Test
    fun `a customer should not read the low stocks`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/stocks/low").with(asUser(EXISTING_CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isForbidden)
    }
}
