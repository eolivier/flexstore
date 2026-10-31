package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaDiscountCodeRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.Instant

private const val CUSTOMER_ID = "it-discount-customer"

@ExtendWith(RefilledStocks::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class DiscountCodeIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @Autowired
    private lateinit var cartRepository: JpaCartRepository

    @Autowired
    private lateinit var cartItemRepository: JpaCartItemRepository

    @Autowired
    private lateinit var discountCodeRepository: JpaDiscountCodeRepository

    private val nextYear = Instant.now().plusSeconds(365L * 24 * 3600).toString()

    @BeforeEach
    fun createCustomer() {
        cleanUp()
        userRepository.save(UserEntity(CUSTOMER_ID, "Jane", "Doe", "it-discount-customer@example.com", "not-used"))
    }

    @AfterEach
    fun cleanUp() {
        jdbcTemplate.deleteOrdersOf(CUSTOMER_ID)
        discountCodeRepository.deleteAll(discountCodeRepository.findAll().filter { it.code.startsWith("ITDISC") })
        cartRepository.findByUserId(CUSTOMER_ID)?.let { cart ->
            cartItemRepository.deleteAll(cartItemRepository.findAll().filter { it.cartId == cart.id })
            cartRepository.delete(cart)
        }
        if (userRepository.existsById(CUSTOMER_ID)) userRepository.deleteById(CUSTOMER_ID)
    }

    @Test
    fun `an administrator should give a discount code, and see it among the codes`() {
        // Act
        val response = giveDiscountCode("itdisc10", "PERCENTAGE", 10, nextYear, 5)
        // Assert
        response.andExpect(status().isCreated).andExpect(jsonPath("$.code").value("ITDISC10"))
        mockMvc.perform(get("/api/admin/discount-codes").with(asAdmin()))
            .andExpect(jsonPath("$[?(@.code == 'ITDISC10')].timesUsed").value(0))
    }

    @Test
    fun `an invalid or duplicated discount code should be refused`() {
        // Arrange
        giveDiscountCode("ITDISC10", "PERCENTAGE", 10, nextYear, 5).andExpect(status().isCreated)
        // Act
        val duplicated = giveDiscountCode("ITDISC10", "AMOUNT", 5, nextYear, 5)
        val invalid = giveDiscountCode("ITDISCBAD", "PERCENTAGE", 150, nextYear, 5)
        // Assert
        duplicated.andExpect(status().isConflict)
        invalid.andExpect(status().isBadRequest)
    }

    @Test
    fun `a customer should not give a discount code`() {
        // Act
        val response = mockMvc.perform(
            post("/api/admin/discount-codes").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
                .content(newDiscountCode("ITDISC10", "PERCENTAGE", 10, nextYear, 5))
        )
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `an order placed with a discount code should take its reduction off the items`() {
        // Arrange
        giveDiscountCode("ITDISCFIVE", "AMOUNT", 5, nextYear, 5).andExpect(status().isCreated)
        addToCart(productId = "1", quantity = 2)
        // Act
        val response = placeOrderWithCode("itdiscfive")
        // Assert
        response.andExpect(status().isCreated)
            .andExpect(jsonPath("$.discountCode").value("ITDISCFIVE"))
            .andExpect(jsonPath("$.discountAmount").value(5))
            .andExpect(jsonPath("$.totalPrice").value(45))
        mockMvc.perform(get("/api/admin/discount-codes").with(asAdmin()))
            .andExpect(jsonPath("$[?(@.code == 'ITDISCFIVE')].timesUsed").value(1))
    }

    @Test
    fun `the customer should see what a discount code takes off their cart`() {
        // Arrange
        giveDiscountCode("ITDISC10", "PERCENTAGE", 10, nextYear, 5).andExpect(status().isCreated)
        addToCart(productId = "1", quantity = 2)
        // Act
        val response = mockMvc.perform(
            get("/api/orders/discount-preview").param("code", "itdisc10").with(asUser(CUSTOMER_ID))
        )
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.amountOff").value(5))
    }

    @Test
    fun `an unknown, expired or used up discount code should be refused`() {
        // Arrange
        val lastYear = Instant.now().minusSeconds(365L * 24 * 3600).toString()
        giveDiscountCode("ITDISCOLD", "AMOUNT", 5, lastYear, 5).andExpect(status().isCreated)
        giveDiscountCode("ITDISCONCE", "AMOUNT", 5, nextYear, 1).andExpect(status().isCreated)
        addToCart(productId = "1", quantity = 1)
        placeOrderWithCode("ITDISCONCE").andExpect(status().isCreated)
        addToCart(productId = "1", quantity = 1)
        // Act
        val unknown = placeOrderWithCode("ITDISCNOPE")
        val expired = placeOrderWithCode("ITDISCOLD")
        val usedUp = placeOrderWithCode("ITDISCONCE")
        // Assert
        unknown.andExpect(status().isNotFound)
        expired.andExpect(status().isGone)
        usedUp.andExpect(status().isGone)
    }

    @Test
    fun `an administrator should delete a discount code`() {
        // Arrange
        giveDiscountCode("ITDISC10", "PERCENTAGE", 10, nextYear, 5).andExpect(status().isCreated)
        // Act
        val response = mockMvc.perform(delete("/api/admin/discount-codes/ITDISC10").with(asAdmin()))
        // Assert
        response.andExpect(status().isNoContent)
        mockMvc.perform(get("/api/admin/discount-codes").with(asAdmin()))
            .andExpect(jsonPath("$[?(@.code == 'ITDISC10')]").isEmpty)
    }

    private fun newDiscountCode(code: String, type: String, value: Int, expiresAt: String, usageLimit: Int) =
        """{"code": "$code", "reductionType": "$type", "reductionValue": $value,
            "expiresAt": "$expiresAt", "usageLimit": $usageLimit}"""

    private fun giveDiscountCode(code: String, type: String, value: Int, expiresAt: String, usageLimit: Int) =
        mockMvc.perform(
            post("/api/admin/discount-codes").with(asAdmin()).contentType(MediaType.APPLICATION_JSON)
                .content(newDiscountCode(code, type, value, expiresAt, usageLimit))
        )

    private fun addToCart(productId: String, quantity: Int) {
        val item = JsonItem("", productId, "Product", "Description", "CLOTHING", BigDecimal.TEN, "€", quantity)
        mockMvc.perform(
            post("/api/cart/save").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
    }

    private fun placeOrderWithCode(code: String) = mockMvc.perform(
        post("/api/orders").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
            .content(HOME_ADDRESSES.removeSuffix("}") + """, "discountCode": "$code"}""")
    )
}
