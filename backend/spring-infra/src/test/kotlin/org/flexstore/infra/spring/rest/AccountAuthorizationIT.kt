package org.flexstore.infra.spring.rest

import org.flexstore.infra.spring.config.USERS_CACHE
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// A token stays valid until it expires: the token of a user who does not exist, or whose account is suspended,
// locked or disabled, is turned into an anonymous request, refused on the routes that require a signed-in user
@ExtendWith(ExistingCustomer::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AccountAuthorizationIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var cacheManager: CacheManager

    // Written directly in the database, so the cache of the reads of the user table (see CacheConfig) is
    // evicted by hand
    private fun giveStatus(status: String) {
        jdbcTemplate.update("""UPDATE "user" SET status = ? WHERE id = ?""", status, EXISTING_CUSTOMER_ID)
        cacheManager.getCache(USERS_CACHE)?.evict(EXISTING_CUSTOMER_ID)
    }

    @ParameterizedTest
    @ValueSource(strings = ["ACTIVE", "PENDING_VERIFICATION"])
    fun `a customer whose account can sign in should read their orders`(status: String) {
        // Arrange
        giveStatus(status)
        // Act
        val response = mockMvc.perform(get("/api/orders").with(asUser(EXISTING_CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isOk)
    }

    @ParameterizedTest
    @ValueSource(strings = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not read their orders`(status: String) {
        // Arrange
        giveStatus(status)
        // Act
        val response = mockMvc.perform(get("/api/orders").with(asUser(EXISTING_CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    @Test
    fun `a user who does not exist should not read orders`() {
        // Act
        val response = mockMvc.perform(get("/api/orders").with(asUser("it-user-who-does-not-exist")))
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    @ParameterizedTest
    @ValueSource(strings = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should still read the public products`(status: String) {
        // Arrange
        giveStatus(status)
        // Act
        val response = mockMvc.perform(get("/api/products/").with(asUser(EXISTING_CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isOk)
    }
}
