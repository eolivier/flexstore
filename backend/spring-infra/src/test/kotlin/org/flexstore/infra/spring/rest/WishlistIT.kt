package org.flexstore.infra.spring.rest

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val CUSTOMER_ID = "it-wishlist-customer"
private const val OTHER_CUSTOMER_ID = "it-wishlist-other-customer"
private const val PRODUCT_ID = "1"
private const val OTHER_PRODUCT_ID = "2"

@ExtendWith(RefilledStocks::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class WishlistIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @BeforeEach
    fun createCustomers() {
        cleanUp()
        userRepository.save(UserEntity(CUSTOMER_ID, "Jane", "Doe", "it-wishlist-customer@example.com", "unused"))
        userRepository.save(
            UserEntity(OTHER_CUSTOMER_ID, "John", "Roe", "it-wishlist-other@example.com", "unused")
        )
    }

    @AfterEach
    fun cleanUp() {
        jdbcTemplate.update("DELETE FROM wishlist_item WHERE user_id IN (?, ?)", CUSTOMER_ID, OTHER_CUSTOMER_ID)
        listOf(CUSTOMER_ID, OTHER_CUSTOMER_ID).forEach {
            if (userRepository.existsById(it)) userRepository.deleteById(it)
        }
    }

    @Test
    fun `a customer should save a product to their wishlist`() {
        // Act
        val response = save(PRODUCT_ID, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isNoContent)
        wishlist(CUSTOMER_ID).andExpect(jsonPath("$[?(@.id == '$PRODUCT_ID')]").isNotEmpty)
    }

    @Test
    fun `saving a product already saved should leave the wishlist as it is`() {
        // Arrange
        save(PRODUCT_ID, CUSTOMER_ID).andExpect(status().isNoContent)
        // Act
        val response = save(PRODUCT_ID, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isNoContent)
        wishlist(CUSTOMER_ID).andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    fun `a customer should remove a product from their wishlist`() {
        // Arrange
        save(PRODUCT_ID, CUSTOMER_ID).andExpect(status().isNoContent)
        // Act
        val response = mockMvc.perform(delete("/api/wishlist/$PRODUCT_ID").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isNoContent)
        wishlist(CUSTOMER_ID).andExpect(jsonPath("$[?(@.id == '$PRODUCT_ID')]").isEmpty)
    }

    @Test
    fun `removing a product not saved should leave the wishlist as it is`() {
        // Act
        val response = mockMvc.perform(delete("/api/wishlist/$PRODUCT_ID").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isNoContent)
        wishlist(CUSTOMER_ID).andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `an unknown product should not be saved to a wishlist`() {
        // Act
        val response = save("unknown-product", CUSTOMER_ID)
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `a wishlist should hold the products of its customer only`() {
        // Arrange
        save(PRODUCT_ID, CUSTOMER_ID).andExpect(status().isNoContent)
        save(OTHER_PRODUCT_ID, OTHER_CUSTOMER_ID).andExpect(status().isNoContent)
        // Act
        val response = wishlist(CUSTOMER_ID)
        // Assert
        response.andExpect(jsonPath("$[?(@.id == '$PRODUCT_ID')]").isNotEmpty)
            .andExpect(jsonPath("$[?(@.id == '$OTHER_PRODUCT_ID')]").isEmpty)
    }

    @Test
    fun `a visitor should not read a wishlist`() {
        // Act
        val response = mockMvc.perform(get("/api/wishlist"))
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    @Test
    fun `a visitor should not save a product to a wishlist`() {
        // Act
        val response = mockMvc.perform(put("/api/wishlist/$PRODUCT_ID"))
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    private fun save(productId: String, customerId: String) =
        mockMvc.perform(put("/api/wishlist/$productId").with(asUser(customerId)))

    private fun wishlist(customerId: String) =
        mockMvc.perform(get("/api/wishlist").with(asUser(customerId))).andExpect(status().isOk)
}
