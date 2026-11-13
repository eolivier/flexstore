package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.cart.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.cart.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.order.JpaOrderRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.user.JpaUserRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.email.RecordingEmailConfig
import org.flexstore.infra.spring.adapter.email.RecordingEmailSender
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

private const val CUSTOMER_ID = "it-admin-order-customer"
private const val CUSTOMER_EMAIL = "it-admin-order-customer@example.com"

@ExtendWith(RefilledStocks::class)
@Import(RecordingEmailConfig::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AdminOrderIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var orderRepository: JpaOrderRepository

    @Autowired
    private lateinit var cartItemRepository: JpaCartItemRepository

    @Autowired
    private lateinit var cartRepository: JpaCartRepository

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @Autowired
    private lateinit var emailSender: RecordingEmailSender

    @BeforeEach
    fun createCustomer() {
        cleanUp()
        emailSender.clear()
        userRepository.save(UserEntity(CUSTOMER_ID, "Jane", "Doe", CUSTOMER_EMAIL, "not-used"))
    }

    @AfterEach
    fun cleanUp() {
        jdbcTemplate.deleteOrdersOf(CUSTOMER_ID)
        cartRepository.findByUserId(CUSTOMER_ID)?.let { cart ->
            cartItemRepository.deleteAll(cartItemRepository.findAll().filter { it.cartId == cart.id })
            cartRepository.delete(cart)
        }
        if (userRepository.existsById(CUSTOMER_ID)) userRepository.deleteById(CUSTOMER_ID)
    }

    @Test
    fun `only an administrator should list the orders of all the customers`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/orders").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `an administrator should list the orders of the other customers`() {
        // Arrange
        val orderId = placeOrder()
        // Act
        val response = mockMvc.perform(get("/api/admin/orders").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.id == '$orderId')].status").value("PAID"))
            .andExpect(jsonPath("$[?(@.id == '$orderId')].customer.email").value(CUSTOMER_EMAIL))
    }

    @Test
    fun `an administrator should read an order with its customer`() {
        // Arrange
        val orderId = placeOrder()
        // Act
        val response = mockMvc.perform(get("/api/admin/orders/$orderId").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(orderId))
            .andExpect(jsonPath("$.lines.length()").value(1))
            .andExpect(jsonPath("$.customer.firstName").value("Jane"))
            .andExpect(jsonPath("$.customer.lastName").value("Doe"))
            .andExpect(jsonPath("$.customer.email").value(CUSTOMER_EMAIL))
    }

    @Test
    fun `an administrator should read the statuses an order went through, oldest first`() {
        // Arrange
        val orderId = placeOrder()
        advance(orderId, "PAID").andExpect(status().isOk)
        // Act
        val response = mockMvc.perform(get("/api/admin/orders/$orderId").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.statusHistory[*].status")
                .value(contains("AWAITING_PAYMENT", "PAID", "PROCESSING")))
            .andExpect(jsonPath("$.statusHistory[2].changedAt").isNotEmpty)
    }

    @Test
    fun `only an administrator should read the order of another customer`() {
        // Arrange
        val orderId = placeOrder()
        // Act
        val response = mockMvc.perform(get("/api/admin/orders/$orderId").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `reading an unknown order should answer 404`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/orders/unknown-order").with(asAdmin()))
        // Assert
        response.andExpect(status().isNotFound)
    }

    @ParameterizedTest(name = "advancing {0} times should make the order {1}")
    @CsvSource("1, PROCESSING", "2, SHIPPED", "3, DELIVERED")
    fun `an administrator should move an order along its life cycle`(advances: Int, expectedStatus: String) {
        // Arrange
        val orderId = placeOrder()
        val statuses = listOf("PAID", "PROCESSING", "SHIPPED").take(advances)
        statuses.dropLast(1).forEach { advance(orderId, it).andExpect(status().isOk) }
        // Act
        val response = advance(orderId, statuses.last())
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.status").value(expectedStatus))
            .andExpect(jsonPath("$.customer.email").value(CUSTOMER_EMAIL))
    }

    @Test
    fun `the customer should be told by email when their order is shipped, then delivered`() {
        // Arrange
        val orderId = placeOrder()
        listOf("PAID", "PROCESSING").forEach { advance(orderId, it).andExpect(status().isOk) }
        // Act
        val response = advance(orderId, "SHIPPED")
        // Assert
        response.andExpect(status().isOk)
        val statusEmails = emailSender.sentOrderStatusEmails()
        assertThat(statusEmails.map { it.order.status.name }).containsExactly("SHIPPED", "DELIVERED")
        assertThat(statusEmails.map { it.recipient.value }).containsOnly(CUSTOMER_EMAIL)
    }

    @Test
    fun `an administrator should cancel and refund a paid order, its units back in stock`() {
        // Arrange
        val orderId = placeOrder()
        val stockAfterOrder = laptopStock()
        // Act
        val response = refund(orderId, "PAID")
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.status").value("REFUNDED"))
        assertThat(laptopStock()).isEqualTo(stockAfterOrder + 1)
        assertThat(emailSender.sentOrderStatusEmails().map { it.order.status.name }).containsExactly("REFUNDED")
    }

    @Test
    fun `a shipped order should not be refunded`() {
        // Arrange
        val orderId = placeOrder()
        listOf("PAID", "PROCESSING").forEach { advance(orderId, it).andExpect(status().isOk) }
        // Act
        val response = refund(orderId, "SHIPPED")
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `only an administrator should refund an order`() {
        // Arrange
        val orderId = placeOrder()
        // Act
        val response = mockMvc.perform(
            post("/api/admin/orders/$orderId/refund").with(asUser(CUSTOMER_ID))
                .contentType(MediaType.APPLICATION_JSON).content("""{"currentStatus": "PAID"}""")
        )
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `an order whose status changed in the meantime should not move`() {
        // Arrange
        val orderId = placeOrder()
        advance(orderId, "PAID").andExpect(status().isOk)
        // Act
        val response = advance(orderId, "PAID")
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `a delivered order should not move`() {
        // Arrange
        val orderId = placeOrder()
        listOf("PAID", "PROCESSING", "SHIPPED").forEach { advance(orderId, it).andExpect(status().isOk) }
        // Act
        val response = advance(orderId, "DELIVERED")
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `an order awaiting its payment should not be moved by an administrator`() {
        // Arrange
        val orderId = placeOrder(paid = false)
        // Act
        val response = advance(orderId, "AWAITING_PAYMENT")
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `an unknown order should not be found`() {
        // Act
        val response = advance("unknown-order", "PROCESSING")
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `an order shipped with its tracking should show it to the customer and in the email`() {
        // Arrange
        val orderId = placeOrder()
        advance(orderId, "PAID").andExpect(status().isOk)
        // Act
        val response = ship(orderId, "COLISSIMO", "6A12345678901")
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.status").value("SHIPPED"))
            .andExpect(jsonPath("$.tracking.carrier").value("COLISSIMO"))
            .andExpect(jsonPath("$.tracking.trackingNumber").value("6A12345678901"))
            .andExpect(jsonPath("$.tracking.trackingUrl").value(containsString("6A12345678901")))
        mockMvc.perform(get("/api/orders/$orderId").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.tracking.carrier").value("COLISSIMO"))
        val shippedEmail = emailSender.sentOrderStatusEmails().single()
        assertThat(shippedEmail.order.tracking.isGiven()).isTrue()
    }

    @Test
    fun `an order shipped without tracking should have none`() {
        // Arrange
        val orderId = placeOrder()
        listOf("PAID", "PROCESSING").forEach { advance(orderId, it).andExpect(status().isOk) }
        // Act
        val response = mockMvc.perform(get("/api/orders/$orderId").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(jsonPath("$.tracking").doesNotExist())
    }

    @Test
    fun `a carrier without a tracking number should be refused`() {
        // Arrange
        val orderId = placeOrder()
        advance(orderId, "PAID").andExpect(status().isOk)
        // Act
        val response = mockMvc.perform(
            post("/api/admin/orders/$orderId/status/advance").with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"currentStatus": "PROCESSING", "carrier": "UPS"}""")
        )
        // Assert
        response.andExpect(status().isBadRequest)
    }

    @Test
    fun `an administrator should correct the tracking of a shipped order`() {
        // Arrange
        val orderId = placeOrder()
        advance(orderId, "PAID").andExpect(status().isOk)
        ship(orderId, "COLISSIMO", "6A111").andExpect(status().isOk)
        // Act
        val response = correctTracking(orderId, "UPS", "1Z222")
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.tracking.carrier").value("UPS"))
            .andExpect(jsonPath("$.tracking.trackingNumber").value("1Z222"))
    }

    @Test
    fun `the tracking of an order not shipped yet should not be corrected`() {
        // Arrange
        val orderId = placeOrder()
        // Act
        val response = correctTracking(orderId, "UPS", "1Z222")
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `only an administrator should correct a tracking`() {
        // Arrange
        val orderId = placeOrder()
        // Act
        val response = mockMvc.perform(
            put("/api/admin/orders/$orderId/tracking").with(asUser(CUSTOMER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"carrier": "UPS", "trackingNumber": "1Z222"}""")
        )
        // Assert
        response.andExpect(status().isForbidden)
    }

    private fun refund(orderId: String, currentStatus: String) = mockMvc.perform(
        post("/api/admin/orders/$orderId/refund").with(asAdmin())
            .contentType(MediaType.APPLICATION_JSON).content("""{"currentStatus": "$currentStatus"}""")
    )

    // The units of the laptop (product 6) in stock
    private fun laptopStock(): Int {
        val body = mockMvc.perform(get("/api/stocks/6")).andReturn().response.contentAsString
        return objectMapper.readTree(body)["quantity"].asInt()
    }

    private fun advance(orderId: String, currentStatus: String) = mockMvc.perform(
        post("/api/admin/orders/$orderId/status/advance").with(asAdmin())
            .contentType(MediaType.APPLICATION_JSON).content("""{"currentStatus": "$currentStatus"}""")
    )

    private fun ship(orderId: String, carrier: String, trackingNumber: String) = mockMvc.perform(
        post("/api/admin/orders/$orderId/status/advance").with(asAdmin())
            .contentType(MediaType.APPLICATION_JSON).content(
                """{"currentStatus": "PROCESSING", "carrier": "$carrier",
                    "trackingNumber": "$trackingNumber"}"""
            )
    )

    private fun correctTracking(orderId: String, carrier: String, trackingNumber: String) = mockMvc.perform(
        put("/api/admin/orders/$orderId/tracking").with(asAdmin())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"carrier": "$carrier", "trackingNumber": "$trackingNumber"}""")
    )

    // A paid order, unless said otherwise
    private fun placeOrder(paid: Boolean = true): String {
        val item = JsonItem("", "6", "Product", "Description", "ELECTRONICS", BigDecimal.TEN, "€", 1)
        mockMvc.perform(
            post("/api/cart/save").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
        val body = mockMvc.perform(post("/api/orders")
            .withHomeAddresses(mockMvc.currentTermsOfSaleId())
            .with(asUser(CUSTOMER_ID)))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        val orderId = objectMapper.readTree(body)["id"].asText()
        if (paid) mockMvc.pay(orderId, CUSTOMER_ID).andExpect(status().isCreated)
        return orderId
    }

    @Test
    fun `an administrator should list a page of the orders, newest first, with the total in a header`() {
        // Arrange
        val orderId = placeOrder()
        // Act
        val response = mockMvc.perform(get("/api/admin/orders").param("size", "1").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(header().exists("X-Total-Count"))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(orderId))
    }
}
