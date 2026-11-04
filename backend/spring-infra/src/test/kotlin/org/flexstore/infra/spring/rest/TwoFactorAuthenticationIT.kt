package org.flexstore.infra.spring.rest

import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.flexstore.infra.spring.security.ADMIN_ACCOUNT_ID
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

private const val ADMIN_EMAIL = "it-two-factor-admin@example.com"
private const val ADMIN_PASSWORD = "admin-password"
private const val CUSTOMER_EMAIL = "it-two-factor-customer@example.com"
private const val CUSTOMER_PASSWORD = "customer-password"

// End to end: a customer or an administrator enrolls, confirms with the first code, then a sign-in with their
// password is challenged and only completed with the code of their authenticator app; disabling goes back to a
// plain sign-in
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class TwoFactorAuthenticationIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var adminId: String
    private lateinit var customerId: String

    @AfterEach
    fun cleanUp() {
        userRepository.findByEmail(Email(ADMIN_EMAIL)).let { userRepository.delete(it.id) }
        userRepository.findByEmail(Email(CUSTOMER_EMAIL)).let { userRepository.delete(it.id) }
    }

    private fun createEnrolledAdmin(): String {
        val draftJsonUser = DraftJsonUser("Two-factor", "Admin", ADMIN_EMAIL, ADMIN_PASSWORD, "ADMIN")
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), draftJsonUser.toUser()))
        return (userRepository.findByEmail(Email(ADMIN_EMAIL)) as User.DefinedUser).id.value
    }

    private fun createCustomer(): String {
        val draftJsonUser = DraftJsonUser("Two-factor", "Customer", CUSTOMER_EMAIL, CUSTOMER_PASSWORD, "USER")
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), draftJsonUser.toUser()))
        return (userRepository.findByEmail(Email(CUSTOMER_EMAIL)) as User.DefinedUser).id.value
    }

    private fun startEnrollment(userId: String, auth: RequestPostProcessor): String {
        val enrollmentPath = "/api/users/$userId/two-factor-authentication/enrollment"
        return mockMvc.perform(post(enrollmentPath).with(auth))
            .andExpect(status().isOk).andReturn().response.contentAsString
    }

    private fun codeFor(enrollmentResponse: String): String {
        val secret = objectMapper.readTree(enrollmentResponse)["secret"].asText()
        return TwoFactorSecret(secret).codeAt(Instant.now()).value
    }

    @Test
    fun `an administrator can enroll, confirm, and sign in with the code of their authenticator app`() {
        // Arrange: enroll and confirm
        adminId = createEnrolledAdmin()
        val enrollment = startEnrollment(adminId, asAdmin(adminId))
        val provisioningUri = objectMapper.readTree(enrollment)["provisioningUri"].asText()
        assertThat(provisioningUri).startsWith("otpauth://totp/")
        mockMvc.perform(
            post("/api/users/$adminId/two-factor-authentication/confirmation").with(asAdmin(adminId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"code": "${codeFor(enrollment)}"}""")
        ).andExpect(status().isNoContent)

        // Act: a sign-in with the right password is challenged, not signed in right away
        val loginResponse = mockMvc.perform(
            post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "$ADMIN_EMAIL", "password": "$ADMIN_PASSWORD"}""")
        ).andExpect(status().isOk).andReturn().response.contentAsString
        val login = objectMapper.readTree(loginResponse)

        // Assert: the challenge carries no token yet
        assertThat(login["status"].asText()).isEqualTo("TWO_FACTOR_REQUIRED")
        assertThat(login.has("token")).isFalse()
        val challengeId = login["challengeId"].asText()

        // Act: completing the challenge with the code of the authenticator app signs the administrator in
        val secret = TwoFactorSecret(objectMapper.readTree(enrollment)["secret"].asText())
        val verification = mockMvc.perform(
            post("/api/users/login/two-factor-authentication").contentType(MediaType.APPLICATION_JSON)
                .content("""{"challengeId": "$challengeId", "code": "${secret.codeAt(Instant.now()).value}"}""")
        ).andExpect(status().isOk).andReturn().response.contentAsString
        val session = objectMapper.readTree(verification)

        // Assert
        assertThat(session["status"].asText()).isEqualTo("SIGNED_IN")
        assertThat(session["token"].asText()).isNotEmpty()
        assertThat(session["user"]["email"].asText()).isEqualTo(ADMIN_EMAIL)
    }

    @Test
    fun `a wrong code should not complete the challenge`() {
        // Arrange
        adminId = createEnrolledAdmin()
        val enrollment = startEnrollment(adminId, asAdmin(adminId))
        mockMvc.perform(
            post("/api/users/$adminId/two-factor-authentication/confirmation").with(asAdmin(adminId))
                .contentType(MediaType.APPLICATION_JSON).content("""{"code": "${codeFor(enrollment)}"}""")
        ).andExpect(status().isNoContent)
        val loginResponse = mockMvc.perform(
            post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "$ADMIN_EMAIL", "password": "$ADMIN_PASSWORD"}""")
        ).andReturn().response.contentAsString
        val challengeId = objectMapper.readTree(loginResponse)["challengeId"].asText()
        // Act - Assert
        mockMvc.perform(
            post("/api/users/login/two-factor-authentication").contentType(MediaType.APPLICATION_JSON)
                .content("""{"challengeId": "$challengeId", "code": "000000"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `disabling two-factor authentication lets the administrator sign in without a challenge`() {
        // Arrange
        adminId = createEnrolledAdmin()
        val enrollment = startEnrollment(adminId, asAdmin(adminId))
        mockMvc.perform(
            post("/api/users/$adminId/two-factor-authentication/confirmation").with(asAdmin(adminId))
                .contentType(MediaType.APPLICATION_JSON).content("""{"code": "${codeFor(enrollment)}"}""")
        ).andExpect(status().isNoContent)
        // Act
        mockMvc.perform(
            delete("/api/users/$adminId/two-factor-authentication").with(asAdmin(adminId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"currentPassword": "$ADMIN_PASSWORD"}""")
        ).andExpect(status().isNoContent)
        // Assert
        val loginResponse = mockMvc.perform(
            post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "$ADMIN_EMAIL", "password": "$ADMIN_PASSWORD"}""")
        ).andExpect(status().isOk).andReturn().response.contentAsString
        assertThat(objectMapper.readTree(loginResponse)["status"].asText()).isEqualTo("SIGNED_IN")
    }

    @Test
    fun `a customer can enroll, confirm, and sign in with the code of their authenticator app`() {
        // Arrange: enroll and confirm
        customerId = createCustomer()
        val enrollment = startEnrollment(customerId, asUser(customerId))
        val provisioningUri = objectMapper.readTree(enrollment)["provisioningUri"].asText()
        assertThat(provisioningUri).startsWith("otpauth://totp/")
        mockMvc.perform(
            post("/api/users/$customerId/two-factor-authentication/confirmation").with(asUser(customerId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"code": "${codeFor(enrollment)}"}""")
        ).andExpect(status().isNoContent)

        // Act: a sign-in with the right password is challenged, not signed in right away
        val loginResponse = mockMvc.perform(
            post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "$CUSTOMER_EMAIL", "password": "$CUSTOMER_PASSWORD"}""")
        ).andExpect(status().isOk).andReturn().response.contentAsString
        val login = objectMapper.readTree(loginResponse)

        // Assert: the challenge carries no token yet
        assertThat(login["status"].asText()).isEqualTo("TWO_FACTOR_REQUIRED")
        assertThat(login.has("token")).isFalse()
        val challengeId = login["challengeId"].asText()

        // Act: completing the challenge with the code of the authenticator app signs the customer in
        val secret = TwoFactorSecret(objectMapper.readTree(enrollment)["secret"].asText())
        val verification = mockMvc.perform(
            post("/api/users/login/two-factor-authentication").contentType(MediaType.APPLICATION_JSON)
                .content("""{"challengeId": "$challengeId", "code": "${secret.codeAt(Instant.now()).value}"}""")
        ).andExpect(status().isOk).andReturn().response.contentAsString
        val session = objectMapper.readTree(verification)

        // Assert
        assertThat(session["status"].asText()).isEqualTo("SIGNED_IN")
        assertThat(session["token"].asText()).isNotEmpty()
        assertThat(session["user"]["email"].asText()).isEqualTo(CUSTOMER_EMAIL)
    }
}
