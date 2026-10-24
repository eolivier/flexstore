package org.flexstore.infra.spring.rest

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

private const val ADMIN_ID = "security-it-admin"
private const val USER_ID = "security-it-user"
private const val PASSWORD = "testpassword"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun createUsers() {
        userRepository.save(User.DefinedUser(UserId.of(ADMIN_ID), Name("Security Admin"), Email("security.admin@example.com"), PlainPassword(PASSWORD), Role.ADMIN))
        userRepository.save(User.DefinedUser(UserId.of(USER_ID), Name("Security User"), Email("security.user@example.com"), PlainPassword(PASSWORD)))
    }

    @AfterEach
    fun deleteUsers() {
        userRepository.delete(UserId.of(ADMIN_ID))
        userRepository.delete(UserId.of(USER_ID))
    }

    private fun tokenOf(email: String): String {
        val response = login(email, PASSWORD).andExpect(status().isOk).andReturn().response.contentAsString
        return objectMapper.readTree(response).get("token").asText()
    }

    private fun bearer(token: String) = "Bearer $token"

    @Test
    fun `should return the token and the user without password on login`() {
        // Act
        val result = login("security.admin@example.com", PASSWORD)
        // Assert
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.expiresAt").isNotEmpty)
            .andExpect(jsonPath("$.user.id").value(ADMIN_ID))
            .andExpect(jsonPath("$.user.role").value("ADMIN"))
            .andExpect(jsonPath("$.user.password").doesNotExist())
    }

    @ParameterizedTest(name = "{0} / {1}: invalid credentials")
    @CsvSource("unknown@example.com, $PASSWORD", "security.user@example.com, wrong-password")
    fun `should answer the same way for an unknown email and a wrong password`(email: String, password: String) {
        // Act
        val result = login(email, password)
        // Assert
        result.andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Invalid credentials."))
    }

    @ParameterizedTest(name = "GET {0} is public")
    @ValueSource(strings = ["/api/products/", "/api/products/1", "/api/cart/cart-items"])
    fun `should keep the catalog and the cart public`(path: String) {
        // Act
        val result = mockMvc.perform(get(path))
        // Assert
        result.andExpect(status().isOk)
    }

    @Test
    fun `should reject anonymous requests to the users`() {
        // Act
        val result = mockMvc.perform(get("/api/users"))
        // Assert
        result.andExpect(status().isUnauthorized)
    }

    @Test
    fun `should reject anonymous product creation`() {
        // Act
        val result = mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
        // Assert
        result.andExpect(status().isUnauthorized)
    }

    @Test
    fun `should reject an invalid token`() {
        // Act
        val result = mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, bearer("not-a-token")))
        // Assert
        result.andExpect(status().isUnauthorized)
    }

    @Test
    fun `should forbid user management to a user without the admin role`() {
        // Arrange
        val userToken = tokenOf("security.user@example.com")
        // Act
        val result = mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
        // Assert
        result.andExpect(status().isForbidden)
    }

    @Test
    fun `should allow user management to an admin`() {
        // Arrange
        val adminToken = tokenOf("security.admin@example.com")
        // Act
        val result = mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
        // Assert
        result.andExpect(status().isOk)
    }

    @Test
    fun `a user should not change the password of another user`() {
        // Arrange
        val userToken = tokenOf("security.user@example.com")
        // Act
        val result = changePassword(ADMIN_ID, userToken)
        // Assert
        result.andExpect(status().isForbidden)
    }

    @Test
    fun `a user should change their own password`() {
        // Arrange
        val userToken = tokenOf("security.user@example.com")
        // Act
        val result = changePassword(USER_ID, userToken)
        // Assert
        result.andExpect(status().isNoContent)
    }

    private fun login(email: String, password: String) = mockMvc.perform(
        post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email": "$email", "password": "$password"}""")
    )

    private fun changePassword(userId: String, token: String) = mockMvc.perform(
        post("/api/users/$userId/password")
            .header(HttpHeaders.AUTHORIZATION, bearer(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"currentPassword": "$PASSWORD", "newPassword": "another-password"}""")
    )
}
