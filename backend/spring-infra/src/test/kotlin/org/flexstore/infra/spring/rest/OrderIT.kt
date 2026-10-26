package org.flexstore.infra.spring.rest

import org.flexstore.domain.order.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaOrderRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

// Orders and carts reference existing users: the administration account and a user created by the test
private const val CUSTOMER_ID = "u-admin"
private const val OTHER_CUSTOMER_ID = "it-order-customer"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class OrderIT {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

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
    fun refillStocks() = jdbcTemplate.refillStocks()

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
        val response = mockMvc.perform(post("/api/orders").withHomeAddresses())
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    @Test
    fun `should place an order from the cart`() {
        // Arrange
        addToCart(CUSTOMER_ID, productId = "6", quantity = 2)
        addToCart(CUSTOMER_ID, productId = "9", quantity = 1)
        // Act
        val response = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"))
            .andExpect(jsonPath("$.lines.length()").value(2))
            .andExpect(jsonPath("$.lines[0].productName").value("Laptop"))
            .andExpect(jsonPath("$.lines[0].quantity").value(2))
            .andExpect(jsonPath("$.lines[0].linePrice").value(1598))
            .andExpect(jsonPath("$.totalPrice").value(1637))
            .andExpect(jsonPath("$.currency").value("€"))
    }

    @Test
    fun `an order awaiting its payment should give the deadline to pay it, after the payment delay`() {
        // Arrange
        val orderId = placeOrder(CUSTOMER_ID, productId = "6")
        // Act
        val body = mockMvc.perform(get("/api/orders/$orderId").with(asUser(CUSTOMER_ID)))
            .andExpect(status().isOk).andReturn().response.contentAsString
        // Assert
        val order = objectMapper.readTree(body)
        val placedAt = Instant.parse(order["placedAt"].asText())
        val paymentDeadline = Instant.parse(order["paymentDeadline"].asText())
        assertThat(Duration.between(placedAt, paymentDeadline)).isEqualTo(Duration.ofMinutes(10))
    }

    @Test
    fun `a paid order should have no payment deadline`() {
        // Arrange
        val orderId = placeOrder(CUSTOMER_ID, productId = "6")
        mockMvc.pay(orderId, CUSTOMER_ID).andExpect(status().isCreated)
        // Act
        val response = mockMvc.perform(get("/api/orders/$orderId").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.paymentDeadline").isEmpty)
    }

    @Test
    fun `placing an order should keep the cart until the order is paid`() {
        // Arrange
        addToCart(CUSTOMER_ID, productId = "6", quantity = 1)
        // Act
        mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID))).andExpect(status().isCreated)
        // Assert
        mockMvc.perform(get("/api/cart/cart-items").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.items.length()").value(1))
    }

    @Test
    fun `paying an order should make it paid and empty the cart`() {
        // Arrange
        val orderId = placeOrder(CUSTOMER_ID, productId = "6")
        // Act
        val response = mockMvc.pay(orderId, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isCreated).andExpect(jsonPath("$.outcome").value("PAID"))
        mockMvc.perform(get("/api/orders/$orderId").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.status").value("PAID"))
        mockMvc.perform(get("/api/cart/cart-items").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.items.length()").value(0))
    }

    @Test
    fun `a paid order should not be paid again`() {
        // Arrange
        val orderId = placeOrder(CUSTOMER_ID, productId = "6")
        mockMvc.pay(orderId, CUSTOMER_ID).andExpect(status().isCreated)
        // Act
        val response = mockMvc.pay(orderId, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `the order of another customer should not be paid`() {
        // Arrange
        val orderId = placeOrder(OTHER_CUSTOMER_ID, productId = "6")
        // Act
        val response = mockMvc.pay(orderId, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `the Stripe webhook should not exist without a Stripe key`() {
        // Act
        val response = mockMvc.perform(
            post("/api/payments/stripe/webhook").header("Stripe-Signature", "t=1,v1=fake")
                .contentType(MediaType.APPLICATION_JSON).content("{}")
        )
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `an order should be billed to its delivery address when no billing address is given`() {
        // Arrange
        addToCart(CUSTOMER_ID, productId = "6", quantity = 1)
        // Act
        val response = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isCreated)
            .andExpect(jsonPath("$.deliveryAddress.city").value("Paris"))
            .andExpect(jsonPath("$.deliveryAddress.complement").value("Bâtiment B"))
            .andExpect(jsonPath("$.billingAddress.city").value("Paris"))
    }

    @Test
    fun `an order should keep a billing address different from its delivery address`() {
        // Arrange
        addToCart(CUSTOMER_ID, productId = "6", quantity = 1)
        val addresses = """{"deliveryAddress": {"recipientType": "PRIVATE_INDIVIDUAL",
            "firstName": "Jane", "lastName": "Doe",
                "street": "12 rue des Lilas", "postalCode": "75011", "city": "Paris", "country": "France"},
            "billingAddress": {"recipientType": "LEGAL_ENTITY", "companyName": "Acme",
                "vatNumber": "FR12345678901",
                "street": "3 place du Marché", "postalCode": "69001", "city": "Lyon", "country": "France"}}"""
        val orderId = objectMapper.readTree(
            mockMvc.perform(post("/api/orders").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON).content(addresses))
                .andExpect(status().isCreated).andReturn().response.contentAsString
        )["id"].asText()
        // Act
        val response = mockMvc.perform(get("/api/orders/$orderId").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(jsonPath("$.deliveryAddress.city").value("Paris"))
            .andExpect(jsonPath("$.billingAddress.city").value("Lyon"))
            .andExpect(jsonPath("$.billingAddress.companyName").value("Acme"))
            .andExpect(jsonPath("$.billingAddress.recipientType").value("LEGAL_ENTITY"))
            .andExpect(jsonPath("$.billingAddress.vatNumber").value("FR12345678901"))
    }

    @Test
    fun `an order should not be placed with an incomplete address`() {
        // Arrange
        addToCart(CUSTOMER_ID, productId = "6", quantity = 1)
        val addresses = """{"deliveryAddress": {"recipientType": "PRIVATE_INDIVIDUAL",
            "firstName": "Jane", "lastName": "Doe",
            "street": "12 rue des Lilas", "postalCode": "75011", "city": "", "country": "France"}}"""
        // Act
        val response = mockMvc.perform(post("/api/orders").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON).content(addresses))
        // Assert
        response.andExpect(status().isBadRequest)
    }

    @Test
    fun `should not place an order from an empty cart`() {
        // Act
        val response = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
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
        val body = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(customerId)))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        return objectMapper.readTree(body)["id"].asText()
    }
}
