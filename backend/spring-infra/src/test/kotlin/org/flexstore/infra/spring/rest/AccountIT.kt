package org.flexstore.infra.spring.rest

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.jpa.JpaUserRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val CUSTOMER_EMAIL = "account-it.jane@example.com"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AccountIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @BeforeEach
    @AfterEach
    fun deleteCustomer() {
        userRepository.findByEmail(CUSTOMER_EMAIL)?.let { userRepository.delete(it) }
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
        val address = stored.addresses.single()
        assertThat(stored.phoneNumber).isEqualTo("+33 6 12 34 56 78")
        assertThat(listOf(address.street, address.complement, address.postalCode, address.city, address.country))
            .containsExactly("12 rue des Lilas", null, "75011", "Paris", "France")
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

    private fun register(registration: String) =
        mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(registration))

    private fun registration(password: String = "password123", phoneNumber: String = "+33 6 12 34 56 78", city: String = "Paris") = """
        {"firstName": "Jane", "lastName": "Doe", "email": "$CUSTOMER_EMAIL", "password": "$password",
         "phoneNumber": "$phoneNumber",
         "postalAddress": {"street": "12 rue des Lilas", "complement": "", "postalCode": "75011", "city": "$city", "country": "France"}}
    """
}
