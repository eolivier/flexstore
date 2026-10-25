package org.flexstore.infra.spring.rest

import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.security.ADMIN_ACCOUNT_ID
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val CUSTOMER_ID = "account-status-it-jane"
private const val CUSTOMER_EMAIL = "account-status-it.jane@example.com"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AccountStatusIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun createCustomer() {
        userRepository.save(User.DefinedUser(UserId.ValidUserId(CUSTOMER_ID), FullName(FirstName("Jane"), LastName("Doe")), Email(CUSTOMER_EMAIL), PlainPassword("password123")))
    }

    @AfterEach
    fun deleteCustomer() {
        userRepository.delete(UserId.ValidUserId(CUSTOMER_ID))
    }

    private fun changeStatus(status: String, userId: String = CUSTOMER_ID) = mockMvc.perform(
        put("/api/users/$userId/status").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content("""{"status": "$status"}""")
    )

    private fun signIn() = mockMvc.perform(
        post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content("""{"email": "$CUSTOMER_EMAIL", "password": "password123"}""")
    )

    @Test
    fun `an administrator should change the status of an account`() {
        // Act
        val response = changeStatus("SUSPENDED")
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.status").value("SUSPENDED"))
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a suspended, locked or disabled account should not sign in, and should know why`(
        accountStatus: String
    ) {
        // Arrange
        changeStatus(accountStatus).andExpect(status().isOk)
        // Act
        val response = signIn()
        // Assert
        response.andExpect(status().isForbidden).andExpect(jsonPath("$.status").value(accountStatus))
    }

    @Test
    fun `a reactivated account should sign in again`() {
        // Arrange
        changeStatus("LOCKED").andExpect(status().isOk)
        changeStatus("ACTIVE").andExpect(status().isOk)
        // Act
        val response = signIn()
        // Assert
        response.andExpect(status().isOk)
    }

    @Test
    fun `a suspended customer should not order, even with a token issued before`() {
        // Arrange
        changeStatus("SUSPENDED").andExpect(status().isOk)
        // Act
        val response = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isForbidden).andExpect(jsonPath("$.status").value("SUSPENDED"))
    }

    @ParameterizedTest(name = "status \"{0}\" -> 422")
    @ValueSource(strings = ["PENDING_VERIFICATION", "UNKNOWN"])
    fun `an administrator should only choose ACTIVE, SUSPENDED, LOCKED or DISABLED`(accountStatus: String) {
        // Act
        val response = changeStatus(accountStatus)
        // Assert
        response.andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `an administrator should not change the status of their own account`() {
        // Act
        val response = changeStatus("DISABLED", ADMIN_ACCOUNT_ID)
        // Assert
        response.andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `a user without the admin role should not change a status`() {
        // Act
        val response = mockMvc.perform(
            put("/api/users/$CUSTOMER_ID/status").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON).content("""{"status": "ACTIVE"}""")
        )
        // Assert
        response.andExpect(status().isForbidden)
    }
}
