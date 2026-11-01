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
        private val streetAddress = StreetAddress(street, complement)
        private val parisTown = Town(PostalCode("75011"), paris)
        private val france = Country("France")
        private val location = Location(streetAddress, Locality(parisTown, france))
        private val address = PostalAddress(PrivateIndividual(janeDoe), location)

        private val noPostalCode = Location(streetAddress, Locality(Town(PostalCode(""), paris), france))
        private val noStreet = Location(StreetAddress(Street("")), Locality(parisTown, france))
        private val noCity = Location(streetAddress, Locality(Town(PostalCode("75011"), City("")), france))
        private val noCountry = Location(streetAddress, Locality(parisTown, Country("")))
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

        private val acme = Company(CompanyName("Acme"), VatNumber("FR32123456789"))
        val acmeBuyer = CustomerType.Business(CustomerCompany(acme, Siren("123456789")))

        @JvmStatic
        fun incompleteRegistrations() = listOf(
            Arguments.of("first name", jane.copy(fullName = FullName(FirstName(" "), LastName("Doe")))),
            Arguments.of("last name", jane.copy(fullName = FullName(FirstName("Jane"), LastName("")))),
            Arguments.of("email", jane.copy(email = Email(""))),
            Arguments.of("phone number", jane.copy(phoneNumber = PhoneNumber(""))),
            Arguments.of("street", jane.copy(postalAddress = addressAt(noStreet))),
            Arguments.of("postal code", jane.copy(postalAddress = addressAt(noPostalCode))),
            Arguments.of("city", jane.copy(postalAddress = addressAt(noCity))),
            Arguments.of("country", jane.copy(postalAddress = addressAt(noCountry))),
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
        val locationWithoutComplement = Location(StreetAddress(street), Locality(parisTown, france))
        val withoutComplement = jane.copy(postalAddress = addressAt(locationWithoutComplement))
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

    @Test
    fun `a customer who creates an account for their company should keep the company`() {
        // Act
        useCase.unfold(jane.copy(customerType = acmeBuyer))
        // Assert
        assertThat(stored().customerType).isEqualTo(acmeBuyer)
    }

    @Test
    fun `the address of a company should be sent to the company, for the attention of the customer`() {
        // Act
        useCase.unfold(jane.copy(customerType = acmeBuyer))
        // Assert
        val recipient = stored().postalAddresses.asList().single().recipient
        assertThat(recipient).isEqualTo(LegalEntity(acme, janeDoe))
    }

    @Test
    fun `an invalid company should be refused`() {
        // Arrange
        val noSiren = CustomerType.Business(CustomerCompany(acme, Siren("")))
        // Act
        val thrown = catchThrowable { useCase.unfold(jane.copy(customerType = noSiren)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidCompanyException::class.java)
        assertThat(userRepository.exists(jane.userId)).isFalse()
    }
}
