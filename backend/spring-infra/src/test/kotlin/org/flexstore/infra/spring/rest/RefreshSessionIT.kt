package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val USER_ID = "refresh-session-it-user"
private const val EMAIL = "refresh.session.it@example.com"
private const val PASSWORD = "testpassword"

// A refresh token renews the access once, signing out ends the session, and a token used twice ends them all
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class RefreshSessionIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    private val user = User.DefinedUser(
        UserId.ValidUserId(USER_ID), FullName(FirstName("Jane"), LastName("Doe")), Email(EMAIL),
        PlainPassword(PASSWORD)
    )

    @BeforeEach
    fun createUser() {
        userRepository.delete(user.id)
        userRepository.save(user)
    }

    // The sessions of the user are deleted with them
    @AfterEach
    fun deleteUser() {
        userRepository.delete(user.id)
    }

    private fun jsonOf(response: ResultActions) =
        objectMapper.readTree(response.andReturn().response.contentAsString)

    private fun signIn() = jsonOf(
        mockMvc.perform(
            post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "$EMAIL", "password": "$PASSWORD"}""")
        ).andExpect(status().isOk)
    )

    private fun refresh(refreshToken: String) = mockMvc.perform(
        post("/api/users/refresh").contentType(MediaType.APPLICATION_JSON)
            .content("""{"refreshToken": "$refreshToken"}""")
    )

    private fun logout(refreshToken: String) = mockMvc.perform(
        post("/api/users/logout").contentType(MediaType.APPLICATION_JSON)
            .content("""{"refreshToken": "$refreshToken"}""")
    )

    @Test
    fun `signing in should give a refresh token with its expiry`() {
        // Act
        val session = signIn()
        // Assert
        assertThat(session.get("refreshToken").asText()).isNotBlank()
        assertThat(session.get("refreshExpiresAt").asText()).isNotBlank()
    }

    @Test
    fun `a refresh token should give a new token that opens the routes of the user, and a new refresh token`() {
        // Arrange
        val refreshToken = signIn().get("refreshToken").asText()
        // Act
        val response = refresh(refreshToken)
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.refreshToken").isNotEmpty)
        val renewed = jsonOf(response)
        assertThat(renewed.get("refreshToken").asText()).isNotEqualTo(refreshToken)
        val bearer = "Bearer ${renewed.get("token").asText()}"
        mockMvc.perform(get("/api/accounts/me").header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isOk).andExpect(jsonPath("$.id").value(USER_ID))
    }

    @Test
    fun `a refresh token used twice should be refused and end all the sessions of the user`() {
        // Arrange
        val refreshToken = signIn().get("refreshToken").asText()
        val renewedRefreshToken = jsonOf(refresh(refreshToken)).get("refreshToken").asText()
        // Act
        val replay = refresh(refreshToken)
        // Assert
        replay.andExpect(status().isUnauthorized)
        refresh(renewedRefreshToken).andExpect(status().isUnauthorized)
    }

    @Test
    fun `an unknown refresh token should be refused`() {
        // Act
        val response = refresh("unknown-refresh-token")
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    @Test
    fun `signing out should end the session`() {
        // Arrange
        val refreshToken = signIn().get("refreshToken").asText()
        // Act
        val response = logout(refreshToken)
        // Assert
        response.andExpect(status().isNoContent)
        refresh(refreshToken).andExpect(status().isUnauthorized)
    }

    @Test
    fun `signing out twice, or with an unknown refresh token, should be no error`() {
        // Arrange
        val refreshToken = signIn().get("refreshToken").asText()
        logout(refreshToken).andExpect(status().isNoContent)
        // Act
        val again = logout(refreshToken)
        val unknown = logout("unknown-refresh-token")
        // Assert
        again.andExpect(status().isNoContent)
        unknown.andExpect(status().isNoContent)
    }

    @Test
    fun `a refresh token of a suspended account should be refused with its status`() {
        // Arrange
        val refreshToken = signIn().get("refreshToken").asText()
        val storedUser = userRepository.findById(user.id) as User.DefinedUser
        userRepository.save(storedUser.copy(status = AccountStatus.SUSPENDED))
        // Act
        val response = refresh(refreshToken)
        // Assert
        response.andExpect(status().isForbidden).andExpect(jsonPath("$.status").value("SUSPENDED"))
    }

    @Test
    fun `a refresh token of a deleted user should be refused`() {
        // Arrange
        val refreshToken = signIn().get("refreshToken").asText()
        userRepository.delete(user.id)
        // Act
        val response = refresh(refreshToken)
        // Assert
        response.andExpect(status().isUnauthorized)
    }
}
