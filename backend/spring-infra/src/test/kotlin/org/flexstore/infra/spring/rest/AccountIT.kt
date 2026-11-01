package org.flexstore.infra.spring.rest

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.adapter.email.RecordingEmailConfig
import org.flexstore.infra.spring.adapter.email.RecordingEmailSender
import org.flexstore.infra.spring.security.asUser
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val CUSTOMER_EMAIL = "account-it.jane@example.com"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(RecordingEmailConfig::class)
class AccountIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @Autowired
    private lateinit var emailSender: RecordingEmailSender

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    @AfterEach
    fun deleteCustomer() {
        userRepository.findByEmail(CUSTOMER_EMAIL)?.let { userRepository.delete(it) }
        emailSender.clear()
    }

    @Test
    fun `a visitor should create their account, which waits for the verification of their email`() {
        // Act
        val response = register(registration())
        // Assert
        response.andExpect(status().isCreated)
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"))
            .andExpect(jsonPath("$.password").doesNotExist())
    }

    @Test
    fun `the new customer should sign in at once`() {
        // Arrange
        register(registration()).andExpect(status().isCreated)
        // Act
        val response = mockMvc.perform(
            post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "$CUSTOMER_EMAIL", "password": "password123"}""")
        )
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.user.status").value("PENDING_VERIFICATION"))
    }

    @Test
    fun `the postal address should be stored with the account`() {
        // Act
        register(registration()).andExpect(status().isCreated)
        // Assert
        val stored = userRepository.findByEmail(CUSTOMER_EMAIL)!!
        val address = stored.addresses.single().address
        assertThat(stored.phoneNumber).isEqualTo("+33 6 12 34 56 78")
        val recipient = listOf(address.firstName, address.lastName, address.companyName)
        val street = listOf(address.street, address.complement)
        val town = listOf(address.postalCode, address.city, address.country)
        assertThat(recipient).containsExactly("Jane", "Doe", null)
        assertThat(street).containsExactly("12 rue des Lilas", null)
        assertThat(town).containsExactly("75011", "Paris", "France")
    }

    @Test
    fun `an email already used should be refused`() {
        // Arrange
        register(registration()).andExpect(status().isCreated)
        // Act
        val response = register(registration())
        // Assert
        response.andExpect(status().isConflict)
    }

    @ParameterizedTest(name = "{0} -> 422")
    @CsvSource(
        "a short password, short, +33 6 12 34 56 78, Paris",
        "no phone number, password123, '', Paris",
        "no city, password123, +33 6 12 34 56 78, ''",
    )
    fun `an invalid registration should be refused`(@Suppress("UNUSED_PARAMETER") case: String, password: String, phoneNumber: String, city: String) {
        // Act
        val response = register(registration(password = password, phoneNumber = phoneNumber, city = city))
        // Assert
        response.andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `a new customer should receive a verification link`() {
        // Act
        register(registration()).andExpect(status().isCreated)
        // Assert
        val recipients = emailSender.sentVerificationLinks().map { it.recipient.value }
        assertThat(recipients).containsExactly(CUSTOMER_EMAIL)
    }

    @Test
    fun `opening the link should activate the account`() {
        // Arrange
        register(registration()).andExpect(status().isCreated)
        // Act
        val response = verify(lastToken())
        // Assert
        response.andExpect(status().isNoContent)
        assertThat(userRepository.findByEmail(CUSTOMER_EMAIL)!!.status).isEqualTo("ACTIVE")
    }

    @Test
    fun `a link should not be used twice`() {
        // Arrange
        register(registration()).andExpect(status().isCreated)
        val token = lastToken()
        verify(token).andExpect(status().isNoContent)
        // Act
        val response = verify(token)
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `a link older than 1 hour should be refused`() {
        // Arrange
        register(registration()).andExpect(status().isCreated)
        val customerId = userRepository.findByEmail(CUSTOMER_EMAIL)!!.id
        jdbcTemplate.update("UPDATE email_verification SET expires_at = now() - interval '1 second' WHERE user_id = ?", customerId)
        // Act
        val response = verify(lastToken())
        // Assert
        response.andExpect(status().isGone)
        assertThat(userRepository.findByEmail(CUSTOMER_EMAIL)!!.status).isEqualTo("PENDING_VERIFICATION")
    }

    @Test
    fun `the token should not be stored in clear`() {
        // Act
        register(registration()).andExpect(status().isCreated)
        // Assert
        val customerId = userRepository.findByEmail(CUSTOMER_EMAIL)!!.id
        val storedHashes = jdbcTemplate.queryForList("SELECT token_hash FROM email_verification WHERE user_id = ?", String::class.java, customerId)
        assertThat(storedHashes).hasSize(1).doesNotContain(lastToken())
    }

    @Test
    fun `a signed-in customer should receive a new link, which expires the previous one`() {
        // Arrange
        register(registration()).andExpect(status().isCreated)
        val previousToken = lastToken()
        val customerId = userRepository.findByEmail(CUSTOMER_EMAIL)!!.id
        // Act
        val response = mockMvc.perform(post("/api/accounts/me/verification-email").with(asUser(customerId)))
        // Assert
        response.andExpect(status().isNoContent)
        assertThat(emailSender.sentVerificationLinks()).hasSize(2)
        verify(previousToken).andExpect(status().isGone)
        verify(lastToken()).andExpect(status().isNoContent)
    }

    @Test
    fun `a new link should need a signed-in customer`() {
        // Act
        val response = mockMvc.perform(post("/api/accounts/me/verification-email"))
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    @Test
    fun `the signed-in customer should read their account, with their phone number and postal address`() {
        // Arrange
        register(registration()).andExpect(status().isCreated)
        val customerId = userRepository.findByEmail(CUSTOMER_EMAIL)!!.id
        // Act
        val response = mockMvc.perform(get("/api/accounts/me").with(asUser(customerId)))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("\$.phoneNumber").value("+33 6 12 34 56 78"))
            .andExpect(jsonPath("\$.postalAddresses[0].city").value("Paris"))
            .andExpect(jsonPath("\$.password").doesNotExist())
    }

    @Test
    fun `a customer who buys for their company should find it in their account`() {
        // Arrange
        register(companyRegistration(siren = "123 456 789", vatNumber = "fr 32 123456789")).andExpect(status().isCreated)
        val customerId = userRepository.findByEmail(CUSTOMER_EMAIL)!!.id
        // Act
        val response = mockMvc.perform(get("/api/accounts/me").with(asUser(customerId)))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("\$.company.companyName").value("Acme"))
            .andExpect(jsonPath("\$.company.siren").value("123456789"))
            .andExpect(jsonPath("\$.company.vatNumber").value("FR32123456789"))
            .andExpect(jsonPath("\$.postalAddresses[0].recipientType").value("LEGAL_ENTITY"))
            .andExpect(jsonPath("\$.postalAddresses[0].companyName").value("Acme"))
            .andExpect(jsonPath("\$.postalAddresses[0].lastName").value("Doe"))
    }

    @Test
    fun `a customer who buys for themselves should have no company`() {
        // Arrange
        register(registration()).andExpect(status().isCreated)
        val customerId = userRepository.findByEmail(CUSTOMER_EMAIL)!!.id
        // Act
        val response = mockMvc.perform(get("/api/accounts/me").with(asUser(customerId)))
        // Assert
        response.andExpect(jsonPath("\$.company").doesNotExist())
    }

    @ParameterizedTest(name = "SIREN {0} and VAT number {1} are refused")
    @CsvSource("12345678, ''", "123456789, FR32987654321")
    fun `an invalid company should be refused`(siren: String, vatNumber: String) {
        // Act
        val response = register(companyRegistration(siren = siren, vatNumber = vatNumber))
        // Assert
        response.andExpect(status().isBadRequest).andExpect(jsonPath("\$.reason").value("INVALID_COMPANY"))
        assertThat(userRepository.findByEmail(CUSTOMER_EMAIL)).isNull()
    }

    private fun companyRegistration(siren: String, vatNumber: String) = registration().trimEnd().removeSuffix("}") +
        """, "company": {"companyName": "Acme", "siren": "$siren", "vatNumber": "$vatNumber"}}"""

    private fun lastToken() = emailSender.sentVerificationLinks().last().token.value

    private fun verify(token: String) = mockMvc.perform(
        post("/api/accounts/email-verification").contentType(MediaType.APPLICATION_JSON).content("""{"token": "$token"}""")
    )

    private fun register(registration: String) =
        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(registration))

    private fun registration(password: String = "password123", phoneNumber: String = "+33 6 12 34 56 78", city: String = "Paris") = """
        {"firstName": "Jane", "lastName": "Doe", "email": "$CUSTOMER_EMAIL", "password": "$password",
         "phoneNumber": "$phoneNumber",
         "postalAddress": {"recipientType": "PRIVATE_INDIVIDUAL",
             "firstName": "Jane", "lastName": "Doe",
            "street": "12 rue des Lilas", "complement": "",
            "postalCode": "75011", "city": "$city", "country": "France"}}
    """
}
