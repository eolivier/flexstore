package org.flexstore.domain.user.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class RegisterCustomerUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val useCase = RegisterCustomerUseCase(userRepository)
    private val userService = UserService(userRepository)

    companion object {
        private val address = PostalAddress(Street("12 rue des Lilas"), AddressComplement("Bâtiment B"), PostalCode("75011"), City("Paris"), Country("France"))

        val jane = CustomerRegistration(
            UserId.ValidUserId("jane-1"),
            FullName(FirstName("Jane"), LastName("Doe")),
            Email("jane.doe@example.com"),
            PlainPassword("password123"),
            PhoneNumber("+33 6 12 34 56 78"),
            address
        )

        @JvmStatic
        fun incompleteRegistrations() = listOf(
            Arguments.of("first name", jane.copy(fullName = FullName(FirstName(" "), LastName("Doe")))),
            Arguments.of("last name", jane.copy(fullName = FullName(FirstName("Jane"), LastName("")))),
            Arguments.of("email", jane.copy(email = Email(""))),
            Arguments.of("phone number", jane.copy(phoneNumber = PhoneNumber(""))),
            Arguments.of("street", jane.copy(postalAddress = address.copy(street = Street("")))),
            Arguments.of("postal code", jane.copy(postalAddress = address.copy(postalCode = PostalCode("")))),
            Arguments.of("city", jane.copy(postalAddress = address.copy(city = City("")))),
            Arguments.of("country", jane.copy(postalAddress = address.copy(country = Country("")))),
        )
    }

    private fun stored() = userRepository.findById(jane.userId) as User.DefinedUser

    @Test
    fun `a registered customer should wait for the verification of their email`() {
        // Act
        useCase.unfold(jane)
        // Assert
        assertThat(stored().status).isEqualTo(AccountStatus.PENDING_VERIFICATION)
        assertThat(stored().role).isEqualTo(Role.USER)
    }

    @Test
    fun `the account should keep the names, the phone number and the postal address`() {
        // Act
        useCase.unfold(jane)
        // Assert
        assertThat(stored().fullName.value).isEqualTo("Jane Doe")
        assertThat(stored().phoneNumber).isEqualTo(PhoneNumber("+33 6 12 34 56 78"))
        assertThat(stored().postalAddresses).isEqualTo(PostalAddresses(address))
    }

    @Test
    fun `the customer should sign in with the chosen password`() {
        // Act
        useCase.unfold(jane)
        // Assert
        assertThat(userRepository.passwordMatches(jane.email, PlainPassword("password123"))).isTrue()
    }

    @Test
    fun `the address complement should be optional`() {
        // Arrange
        val withoutComplement = jane.copy(postalAddress = address.copy(complement = null))
        // Act
        useCase.unfold(withoutComplement)
        // Assert
        assertThat(stored().postalAddresses.asList().single().complement).isNull()
    }

    @ParameterizedTest(name = "a registration without {0} should be refused")
    @MethodSource("incompleteRegistrations")
    fun `an incomplete registration should be refused`(@Suppress("UNUSED_PARAMETER") missing: String, registration: CustomerRegistration) {
        // Act
        val thrown = catchThrowable { useCase.unfold(registration) }
        // Assert
        assertThat(thrown).isInstanceOf(IncompleteRegistrationException::class.java)
        assertThat(userRepository.exists(jane.userId)).isFalse()
    }

    @Test
    fun `a password shorter than 8 characters should be refused`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(jane.copy(password = PlainPassword("short"))) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidPasswordException::class.java)
    }

    @Test
    fun `an email already used should be refused`() {
        // Arrange
        useCase.unfold(jane)
        // Act
        val thrown = catchThrowable { useCase.unfold(jane.copy(userId = UserId.ValidUserId("jane-2"))) }
        // Assert
        assertThat(thrown).isInstanceOf(UserAlreadyExists::class.java)
    }

    @Test
    fun `the user service should return the registered account`() {
        // Act
        val account = userService.registerCustomer(jane) as User.DefinedUser
        // Assert
        assertThat(account.id).isEqualTo(jane.userId)
        assertThat(account.status).isEqualTo(AccountStatus.PENDING_VERIFICATION)
    }
}
