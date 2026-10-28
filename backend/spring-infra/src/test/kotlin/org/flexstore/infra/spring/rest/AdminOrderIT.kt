package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaOrderRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

private const val CUSTOMER_ID = "it-admin-order-customer"
private const val CUSTOMER_EMAIL = "it-admin-order-customer@example.com"

@ExtendWith(RefilledStocks::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AdminOrderIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

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

    @BeforeEach
    fun createCustomer() {
        cleanUp()
        userRepository.save(UserEntity(CUSTOMER_ID, "Jane", "Doe", CUSTOMER_EMAIL, "not-used"))
    }

    @AfterEach
    fun cleanUp() {
        orderRepository.deleteAll(orderRepository.findAllByUserId(CUSTOMER_ID))
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

    private fun advance(orderId: String, currentStatus: String) = mockMvc.perform(
        post("/api/admin/orders/$orderId/status/advance").with(asAdmin())
            .contentType(MediaType.APPLICATION_JSON).content("""{"currentStatus": "$currentStatus"}""")
    )

    // A paid order, unless said otherwise
    private fun placeOrder(paid: Boolean = true): String {
        val item = JsonItem("", "6", "Product", "Description", "ELECTRONICS", BigDecimal.TEN, "€", 1)
        mockMvc.perform(
            post("/api/cart/save").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
        val body = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        val orderId = objectMapper.readTree(body)["id"].asText()
        if (paid) mockMvc.pay(orderId, CUSTOMER_ID).andExpect(status().isCreated)
        return orderId
    }
}
