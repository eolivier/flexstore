package org.flexstore.infra.spring.rest

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.email.RecordingEmailConfig
import org.flexstore.infra.spring.adapter.email.RecordingEmailSender
import org.flexstore.infra.spring.adapter.jpa.JpaUserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val CUSTOMER_EMAIL = "password-reset-it.jane@example.com"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(RecordingEmailConfig::class)
class PasswordResetIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @Autowired
    private lateinit var emailSender: RecordingEmailSender

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun registerCustomer() {
        deleteCustomer()
        val registration = """
            {"firstName": "Jane", "lastName": "Doe", "email": "$CUSTOMER_EMAIL", "password": "old-password",
             "phoneNumber": "+33 6 12 34 56 78",
             "postalAddress": {"recipientType": "PRIVATE_INDIVIDUAL", "firstName": "Jane", "lastName": "Doe",
                "street": "12 rue des Lilas", "complement": "", "postalCode": "75011", "city": "Paris",
                "country": "France"}}
        """
        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(registration))
            .andExpect(status().isCreated)
    }

    @AfterEach
    fun deleteCustomer() {
        userRepository.findByEmail(CUSTOMER_EMAIL)?.let { userRepository.delete(it) }
        emailSender.clear()
    }

    @Test
    fun `a customer who forgot their password should receive a link`() {
        // Act
        val response = requestReset(CUSTOMER_EMAIL)
        // Assert
        response.andExpect(status().isNoContent)
        val recipients = emailSender.sentPasswordResetEmails().map { it.recipient.value }
        assertThat(recipients).containsExactly(CUSTOMER_EMAIL)
    }

    @Test
    fun `an unknown email should get the same answer, and no email`() {
        // Act
        val response = requestReset("nobody@example.com")
        // Assert
        response.andExpect(status().isNoContent)
        assertThat(emailSender.sentPasswordResetEmails()).isEmpty()
    }

    @Test
    fun `the customer should sign in with the new password, and the link should not work again`() {
        // Arrange
        requestReset(CUSTOMER_EMAIL).andExpect(status().isNoContent)
        // Act
        val response = reset(lastToken(), "new-password")
        // Assert
        response.andExpect(status().isNoContent)
        login("new-password").andExpect(status().isOk)
        login("old-password").andExpect(status().isUnauthorized)
        reset(lastToken(), "another-password").andExpect(status().isNotFound)
    }

    @Test
    fun `a new link should replace the previous one`() {
        // Arrange
        requestReset(CUSTOMER_EMAIL).andExpect(status().isNoContent)
        val previousToken = lastToken()
        // Act
        requestReset(CUSTOMER_EMAIL).andExpect(status().isNoContent)
        // Assert
        reset(previousToken, "new-password").andExpect(status().isNotFound)
        reset(lastToken(), "new-password").andExpect(status().isNoContent)
    }

    @Test
    fun `a link older than 1 hour should be refused`() {
        // Arrange
        requestReset(CUSTOMER_EMAIL).andExpect(status().isNoContent)
        val customerId = userRepository.findByEmail(CUSTOMER_EMAIL)!!.id
        val expireLink = "UPDATE password_reset SET expires_at = now() - interval '1 second' WHERE user_id = ?"
        jdbcTemplate.update(expireLink, customerId)
        // Act
        val response = reset(lastToken(), "new-password")
        // Assert
        response.andExpect(status().isGone)
        login("old-password").andExpect(status().isOk)
    }

    @Test
    fun `a password shorter than 8 characters should be refused`() {
        // Arrange
        requestReset(CUSTOMER_EMAIL).andExpect(status().isNoContent)
        // Act
        val response = reset(lastToken(), "short")
        // Assert
        response.andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `the token should not be stored in clear`() {
        // Act
        requestReset(CUSTOMER_EMAIL).andExpect(status().isNoContent)
        // Assert
        val customerId = userRepository.findByEmail(CUSTOMER_EMAIL)!!.id
        val selectHashes = "SELECT token_hash FROM password_reset WHERE user_id = ?"
        val storedHashes = jdbcTemplate.queryForList(selectHashes, String::class.java, customerId)
        assertThat(storedHashes).hasSize(1).doesNotContain(lastToken())
    }

    private fun lastToken() = emailSender.sentPasswordResetEmails().last().reset.expiringToken.token.value

    private fun requestReset(email: String) = mockMvc.perform(
        post("/api/password-reset-requests").contentType(MediaType.APPLICATION_JSON).content("""{"email": "$email"}""")
    )

    private fun reset(token: String, password: String) = mockMvc.perform(
        post("/api/password-resets").contentType(MediaType.APPLICATION_JSON)
            .content("""{"token": "$token", "password": "$password"}""")
    )

    private fun login(password: String) = mockMvc.perform(
        post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
            .content("""{"email": "$CUSTOMER_EMAIL", "password": "$password"}""")
    )
}
