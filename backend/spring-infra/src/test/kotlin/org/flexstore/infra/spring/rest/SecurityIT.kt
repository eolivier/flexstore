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
        val response = mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "$email", "password": "$PASSWORD"}""")
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        return objectMapper.readTree(response).get("token").asText()
    }

    private fun bearer(token: String) = "Bearer $token"

    @Test
    fun `should return the token and the user without password on login`() {
        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "security.admin@example.com", "password": "$PASSWORD"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.expiresAt").isNotEmpty)
            .andExpect(jsonPath("$.user.id").value(ADMIN_ID))
            .andExpect(jsonPath("$.user.role").value("ADMIN"))
            .andExpect(jsonPath("$.user.password").doesNotExist())
    }

    @Test
    fun `should answer the same way for an unknown email and a wrong password`() {
        for (credentials in listOf("unknown@example.com" to PASSWORD, "security.user@example.com" to "wrong-password")) {
            mockMvc.perform(
                post("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email": "${credentials.first}", "password": "${credentials.second}"}""")
            )
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.error").value("Invalid credentials."))
        }
    }

    @Test
    fun `should keep the catalog and the cart public`() {
        mockMvc.perform(get("/api/products/")).andExpect(status().isOk)
        mockMvc.perform(get("/api/products/1")).andExpect(status().isOk)
        mockMvc.perform(get("/api/cart/cart-items")).andExpect(status().isOk)
    }

    @Test
    fun `should reject anonymous requests to protected endpoints`() {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized)
        mockMvc.perform(
            post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{}")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `should reject an invalid token`() {
        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, bearer("not-a-token")))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should forbid user management to a user without the admin role`() {
        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, bearer(tokenOf("security.user@example.com"))))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should allow user management to an admin`() {
        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, bearer(tokenOf("security.admin@example.com"))))
            .andExpect(status().isOk)
    }

    @Test
    fun `should only let users change their own password`() {
        val userToken = tokenOf("security.user@example.com")
        val request = """{"currentPassword": "$PASSWORD", "newPassword": "another-password"}"""
        mockMvc.perform(
            post("/api/users/$ADMIN_ID/password")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ).andExpect(status().isForbidden)
        mockMvc.perform(
            post("/api/users/$USER_ID/password")
                .header(HttpHeaders.AUTHORIZATION, bearer(userToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ).andExpect(status().isNoContent)
    }
}
