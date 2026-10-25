package org.flexstore.domain.user.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class RegisterCustomerUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val useCase = RegisterCustomerUseCase(userRepository)

    companion object {
        private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
        private val street = Street("12 rue des Lilas")
        private val complement = AddressComplement("Bâtiment B")
        private val paris = City("Paris")
        private val location = Location(street, complement, PostalCode("75011"), paris, Country("France"))
        private val address = PostalAddress(PrivateIndividual(janeDoe), location)

        private val noPostalCode = location.copy(postalCode = PostalCode(""))
        private val nobody = PrivateIndividual(FullName(FirstName(""), LastName("")))

        private fun addressAt(location: Location) = PostalAddress(PrivateIndividual(janeDoe), location)

        val jane = CustomerRegistration(
            UserId.ValidUserId("jane-1"),
            janeDoe,
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
            Arguments.of("street", jane.copy(postalAddress = addressAt(location.copy(street = Street(""))))),
            Arguments.of("postal code", jane.copy(postalAddress = addressAt(noPostalCode))),
            Arguments.of("city", jane.copy(postalAddress = addressAt(location.copy(city = City(""))))),
            Arguments.of("country", jane.copy(postalAddress = addressAt(location.copy(country = Country(""))))),
            Arguments.of("recipient", jane.copy(postalAddress = address.copy(recipient = nobody))),
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
        val withoutComplement = jane.copy(postalAddress = addressAt(Location(street, PostalCode("75011"), paris, Country("France"))))
        // Act
        useCase.unfold(withoutComplement)
        // Assert
        assertThat(stored().postalAddresses.asList().single().location.complement).isNull()
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
}
