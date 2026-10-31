package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
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
import java.math.BigDecimal

private const val CUSTOMER_ID = "it-review-customer"
private const val PRODUCT_ID = "1"

@ExtendWith(RefilledStocks::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ReviewIT {

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

    @BeforeEach
    fun createCustomer() {
        cleanUp()
        userRepository.save(UserEntity(CUSTOMER_ID, "Jane", "Doe", "it-review-customer@example.com", "not-used"))
    }

    @AfterEach
    fun cleanUp() {
        jdbcTemplate.update("DELETE FROM product_review WHERE user_id = ?", CUSTOMER_ID)
        jdbcTemplate.deleteOrdersOf(CUSTOMER_ID)
        cartRepository.findByUserId(CUSTOMER_ID)?.let { cart ->
            cartItemRepository.deleteAll(cartItemRepository.findAll().filter { it.cartId == cart.id })
            cartRepository.delete(cart)
        }
        if (userRepository.existsById(CUSTOMER_ID)) userRepository.deleteById(CUSTOMER_ID)
    }

    @Test
    fun `a customer who paid for a product should review it, under their name`() {
        // Arrange
        buyAndPayProduct()
        // Act
        val response = review(5, "Sturdy")
        // Assert
        response.andExpect(status().isCreated)
            .andExpect(jsonPath("$.reviewerName").value("Jane D."))
            .andExpect(jsonPath("$.rating").value(5))
        mockMvc.perform(get("/api/products/$PRODUCT_ID/reviews"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.reviewerName == 'Jane D.')].comment").value("Sturdy"))
    }

    @Test
    fun `the rating of a reviewed product should be public`() {
        // Arrange
        buyAndPayProduct()
        review(4, "Good").andExpect(status().isCreated)
        // Act
        val response = mockMvc.perform(get("/api/product-ratings"))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.productId == '$PRODUCT_ID')].reviewCount").isNotEmpty)
    }

    @Test
    fun `a customer who did not buy the product should be refused`() {
        // Act
        val response = review(5, "Sturdy")
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `a second review of the same product should be refused`() {
        // Arrange
        buyAndPayProduct()
        review(5, "Sturdy").andExpect(status().isCreated)
        // Act
        val response = review(1, "Changed my mind")
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `an invalid rating should be refused`() {
        // Arrange
        buyAndPayProduct()
        // Act
        val response = review(6, "Sturdy")
        // Assert
        response.andExpect(status().isBadRequest)
    }

    @Test
    fun `a visitor should not review a product`() {
        // Act
        val response = mockMvc.perform(
            post("/api/products/$PRODUCT_ID/reviews").contentType(MediaType.APPLICATION_JSON)
                .content("""{"rating": 5, "comment": "Sturdy"}""")
        )
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    private fun buyAndPayProduct() {
        val item = JsonItem("", PRODUCT_ID, "Product", "Description", "CLOTHING", BigDecimal.TEN, "€", 1)
        mockMvc.perform(
            post("/api/cart/save").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
        val placed = mockMvc.perform(post("/api/orders").with(asUser(CUSTOMER_ID)).withHomeAddresses())
            .andExpect(status().isCreated).andReturn()
        val orderId = objectMapper.readTree(placed.response.contentAsString).get("id").asText()
        mockMvc.pay(orderId, CUSTOMER_ID).andExpect(status().isOk)
    }

    private fun review(rating: Int, comment: String) = mockMvc.perform(
        post("/api/products/$PRODUCT_ID/reviews").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
            .content("""{"rating": $rating, "comment": "$comment"}""")
    )
}
