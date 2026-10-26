package org.flexstore.infra.spring.rest

import org.flexstore.domain.settings.*
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ShopSettingsIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var shopSettingsRepository: ShopSettingsRepository

    @AfterEach
    fun restoreSettings() {
        shopSettingsRepository.save(ShopSettings.byDefault())
    }

    @Test
    fun `an admin should read the settings`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/settings").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.orderConfirmationEmail").value("ENABLED"))
    }

    @ParameterizedTest(name = "{0} the order confirmation email -> {1}")
    @CsvSource("disable, DISABLED", "enable, ENABLED")
    fun `an admin should enable or disable the order confirmation email`(
        action: String,
        expectedActivation: String
    ) {
        // Act
        val response = mockMvc.perform(post("/api/admin/settings/order-confirmation-email/$action").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.orderConfirmationEmail").value(expectedActivation))
        mockMvc.perform(get("/api/admin/settings").with(asAdmin()))
            .andExpect(jsonPath("$.orderConfirmationEmail").value(expectedActivation))
    }

    @Test
    fun `an admin should change the time customers have to pay an order`() {
        // Act
        val response = changePaymentDelay(25)
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.paymentDelayMinutes").value(25))
        mockMvc.perform(get("/api/admin/settings").with(asAdmin()))
            .andExpect(jsonPath("$.paymentDelayMinutes").value(25))
    }

    @ParameterizedTest
    @CsvSource("0", "1441")
    fun `a payment delay outside 1 minute to 1 day should be refused`(minutes: Int) {
        // Act
        val response = changePaymentDelay(minutes)
        // Assert
        response.andExpect(status().isBadRequest)
    }

    private fun changePaymentDelay(minutes: Int) = mockMvc.perform(
        put("/api/admin/settings/payment-delay").with(asAdmin()).contentType(MediaType.APPLICATION_JSON)
            .content("""{"minutes": $minutes}""")
    )

    @Test
    fun `a user without the admin role should not read the settings`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/settings").with(asUser("u-admin")))
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `an anonymous visitor should not read the settings`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/settings"))
        // Assert
        response.andExpect(status().isUnauthorized)
    }
}
