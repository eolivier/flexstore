package org.flexstore.infra.spring.rest

import org.flexstore.domain.order.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.infra.spring.adapter.jpa.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.jpa.JpaCartRepository
import org.flexstore.infra.spring.adapter.jpa.JpaOrderRepository
import org.flexstore.infra.spring.adapter.jpa.JpaUserRepository
import org.flexstore.infra.spring.adapter.jpa.UserEntity
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

// Orders and carts reference existing users: the administration account and a user created by the test
private const val CUSTOMER_ID = "u-admin"
private const val OTHER_CUSTOMER_ID = "it-order-customer"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class OrderIT {

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
    fun createOtherCustomer() {
        cleanUp()
        userRepository.save(UserEntity(OTHER_CUSTOMER_ID, "Other", "Customer", "it-order-customer@example.com", "not-used"))
    }

    @AfterEach
    fun cleanUp() {
        listOf(CUSTOMER_ID, OTHER_CUSTOMER_ID).forEach { customerId ->
            orderRepository.deleteAll(orderRepository.findAllByUserId(customerId))
            cartRepository.findByUserId(customerId)?.let { cart ->
                cartItemRepository.deleteAll(cartItemRepository.findAll().filter { it.cartId == cart.id })
                cartRepository.delete(cart)
            }
        }
        userRepository.deleteById(OTHER_CUSTOMER_ID)
    }

    @Test
    fun `placing an order should require a signed-in customer`() {
        // Act
        val response = mockMvc.perform(post("/api/orders"))
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    @Test
    fun `should place an order from the cart`() {
        // Arrange
        addToCart(CUSTOMER_ID, productId = "6", quantity = 2)
        addToCart(CUSTOMER_ID, productId = "9", quantity = 1)
        // Act
        val response = mockMvc.perform(post("/api/orders").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.status").value("PROCESSING"))
            .andExpect(jsonPath("$.lines.length()").value(2))
            .andExpect(jsonPath("$.lines[0].productName").value("Laptop"))
            .andExpect(jsonPath("$.lines[0].quantity").value(2))
            .andExpect(jsonPath("$.lines[0].linePrice").value(1598))
            .andExpect(jsonPath("$.totalPrice").value(1637))
            .andExpect(jsonPath("$.currency").value("€"))
    }

    @Test
    fun `placing an order should empty the cart`() {
        // Arrange
        addToCart(CUSTOMER_ID, productId = "6", quantity = 1)
        // Act
        mockMvc.perform(post("/api/orders").with(asUser(CUSTOMER_ID))).andExpect(status().isCreated)
        // Assert
        mockMvc.perform(get("/api/cart/cart-items").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.items.length()").value(0))
    }

    @Test
    fun `should not place an order from an empty cart`() {
        // Act
        val response = mockMvc.perform(post("/api/orders").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `should list the orders of the customer only, newest first`() {
        // Arrange
        val olderId = placeOrder(CUSTOMER_ID, productId = "1")
        val newerId = placeOrder(CUSTOMER_ID, productId = "2")
        placeOrder(OTHER_CUSTOMER_ID, productId = "3")
        // Act
        val response = mockMvc.perform(get("/api/orders").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(newerId))
            .andExpect(jsonPath("$[1].id").value(olderId))
    }

    @Test
    fun `the order of another customer should not be found`() {
        // Arrange
        val otherOrderId = placeOrder(OTHER_CUSTOMER_ID, productId = "3")
        // Act
        val response = mockMvc.perform(get("/api/orders/$otherOrderId").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isNotFound)
    }

    private fun addToCart(customerId: String, productId: String, quantity: Int) {
        val item = JsonItem(
            itemId = "",
            productId = productId,
            productName = "Product",
            productDescription = "Description",
            productCategory = "ELECTRONICS",
            productPrice = BigDecimal.TEN,
            productCurrency = "€",
            productQuantity = quantity
        )
        mockMvc.perform(
            post("/api/cart/save").with(asUser(customerId)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
    }

    private fun placeOrder(customerId: String, productId: String): String {
        addToCart(customerId, productId, quantity = 1)
        val body = mockMvc.perform(post("/api/orders").with(asUser(customerId)))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        return objectMapper.readTree(body)["id"].asText()
    }
}
