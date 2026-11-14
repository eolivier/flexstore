package org.flexstore.infra.spring.rest

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.shipping.JpaShippingMethodRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.user.JpaUserRepository
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val CUSTOMER_ID = "it-shipping-customer"
private const val ITS_LABEL = "ITSHIP relay point"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ShippingMethodIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @Autowired
    private lateinit var shippingMethodRepository: JpaShippingMethodRepository

    @BeforeEach
    fun createCustomer() {
        cleanUp()
        userRepository.save(
            UserEntity(CUSTOMER_ID, "Jane", "Doe", "it-shipping-customer@example.com", "not-used")
        )
    }

    @AfterEach
    fun cleanUp() {
        shippingMethodRepository.deleteAll(shippingMethodRepository.findAll().filter { it.label.startsWith("ITSHIP") })
        if (userRepository.existsById(CUSTOMER_ID)) userRepository.deleteById(CUSTOMER_ID)
    }

    @Test
    fun `the shop should start with the standard and the express shipping methods`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/shipping-methods").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.id == 'STANDARD')].freeShippingEligibility").value("ELIGIBLE"))
            .andExpect(jsonPath("$[?(@.id == 'EXPRESS')].freeShippingEligibility").value("NOT_ELIGIBLE"))
    }

    @Test
    fun `an administrator should add a shipping method that the customers are then offered`() {
        // Act
        val created = addShippingMethod(ITS_LABEL, 3.5)
        // Assert
        created.andExpect(status().isCreated).andExpect(jsonPath("$.status").value("ENABLED"))
        mockMvc.perform(get("/api/orders/shipping-options").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.options[?(@.label == '$ITS_LABEL')].cost").value(3.5))
    }

    @Test
    fun `a disabled shipping method should no longer be offered to the customers`() {
        // Arrange
        val createdId = idOf(addShippingMethod(ITS_LABEL, 3.5))
        // Act
        val disabled = changeShippingMethod(createdId, ITS_LABEL, 3.5, "DISABLED")
        // Assert
        disabled.andExpect(status().isOk).andExpect(jsonPath("$.status").value("DISABLED"))
        mockMvc.perform(get("/api/orders/shipping-options").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.options[?(@.label == '$ITS_LABEL')]").isEmpty)
        mockMvc.perform(get("/api/admin/shipping-methods").with(asAdmin()))
            .andExpect(jsonPath("$[?(@.id == '$createdId')].status").value("DISABLED"))
    }

    @Test
    fun `an invalid or unknown shipping method should be refused`() {
        // Act
        val withoutName = addShippingMethod("", 3.5)
        val unknown = changeShippingMethod("ITSHIP-unknown", ITS_LABEL, 3.5, "ENABLED")
        // Assert
        withoutName.andExpect(status().isBadRequest)
        unknown.andExpect(status().isNotFound)
    }

    @Test
    fun `a customer should not manage the shipping methods`() {
        // Act
        val response = mockMvc.perform(
            post("/api/admin/shipping-methods").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
                .content(newShippingMethod(ITS_LABEL, 3.5))
        )
        // Assert
        response.andExpect(status().isForbidden)
    }

    private fun newShippingMethod(label: String, cost: Double) =
        """{"label": "$label", "cost": $cost, "freeShippingEligibility": "NOT_ELIGIBLE"}"""

    private fun addShippingMethod(label: String, cost: Double) = mockMvc.perform(
        post("/api/admin/shipping-methods").with(asAdmin()).contentType(MediaType.APPLICATION_JSON)
            .content(newShippingMethod(label, cost))
    )

    private fun idOf(created: org.springframework.test.web.servlet.ResultActions): String {
        val body = created.andReturn().response.contentAsString
        return Regex("\"id\":\"([^\"]+)\"").find(body)!!.groupValues[1]
    }

    private fun changeShippingMethod(id: String, label: String, cost: Double, status: String) = mockMvc.perform(
        put("/api/admin/shipping-methods/$id").with(asAdmin()).contentType(MediaType.APPLICATION_JSON)
            .content(
                """{"label": "$label", "cost": $cost, "freeShippingEligibility": "NOT_ELIGIBLE",
                    "status": "$status"}"""
            )
    )
}
