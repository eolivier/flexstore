package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.order.usecase.UnpaidOrderService
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaOrderRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val CUSTOMER_ID = "it-unpaid-order-customer"
private const val PRODUCT_ID = "6"

@ExtendWith(RefilledStocks::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UnpaidOrderIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var unpaidOrderService: UnpaidOrderService

    @Autowired
    private lateinit var orderRepository: JpaOrderRepository

    @Autowired
    private lateinit var cartRepository: JpaCartRepository

    @Autowired
    private lateinit var cartItemRepository: JpaCartItemRepository

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @BeforeEach
    fun createCustomer() {
        cleanUp()
        userRepository.save(UserEntity(CUSTOMER_ID, "Jane", "Doe", "it-unpaid-order@example.com", "not-used"))
    }

    @AfterEach
    fun cleanUp() {
        orderRepository.deleteAll(orderRepository.findAllByUserId(CUSTOMER_ID))
        cartRepository.findByUserId(CUSTOMER_ID)?.let { cart ->
            cartItemRepository.deleteAll(cartItemRepository.findAll().filter { it.cartId == cart.id })
            cartRepository.delete(cart)
        }
        userRepository.deleteById(CUSTOMER_ID)
    }

    @Test
    fun `a placed order should reserve its units`() {
        // Act
        placeOrder(quantity = 3)
        // Assert
        assertThat(stock()).isEqualTo(97)
    }

    @Test
    fun `an order not paid within the payment delay should be cancelled and give its units back`() {
        // Arrange
        val orderId = placeOrder(quantity = 3)
        placedMinutesAgo(orderId, 10)
        // Act
        unpaidOrderService.cancelLateOrders()
        // Assert
        mockMvc.perform(get("/api/orders/$orderId").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.status").value("CANCELLED"))
        assertThat(stock()).isEqualTo(100)
    }

    @Test
    fun `an order within the payment delay should still await its payment`() {
        // Arrange
        val orderId = placeOrder(quantity = 3)
        placedMinutesAgo(orderId, 9)
        // Act
        unpaidOrderService.cancelLateOrders()
        // Assert
        mockMvc.perform(get("/api/orders/$orderId").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"))
        assertThat(stock()).isEqualTo(97)
    }

    @Test
    fun `a cancelled order should not be paid any more`() {
        // Arrange
        val orderId = placeOrder(quantity = 1)
        placedMinutesAgo(orderId, 10)
        unpaidOrderService.cancelLateOrders()
        // Act
        val response = mockMvc.pay(orderId, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `a paid order should keep its units`() {
        // Arrange
        val orderId = placeOrder(quantity = 3)
        mockMvc.pay(orderId, CUSTOMER_ID).andExpect(status().isCreated)
        placedMinutesAgo(orderId, 60)
        // Act
        unpaidOrderService.cancelLateOrders()
        // Assert
        assertThat(stock()).isEqualTo(97)
    }

    private fun stock() =
        jdbcTemplate.queryForObject("SELECT quantity FROM stock WHERE product_id = ?", Int::class.java, PRODUCT_ID)

    private fun placedMinutesAgo(orderId: String, minutes: Int) {
        val placedAgo = "UPDATE customer_order SET placed_at = now() - make_interval(mins => ?) WHERE id = ?"
        jdbcTemplate.update(placedAgo, minutes, orderId)
    }

    private fun placeOrder(quantity: Int): String {
        val item = """{"itemId": "", "productId": "$PRODUCT_ID", "productName": "Product",
            "productDescription": "Description", "productCategory": "ELECTRONICS", "productPrice": 10,
            "productCurrency": "€", "productQuantity": $quantity}"""
        mockMvc.perform(
            post("/api/cart/save").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON).content(item)
        ).andExpect(status().isOk)
        val body = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        return objectMapper.readTree(body)["id"].asText()
    }
}
