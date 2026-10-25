package org.flexstore.domain.user

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test

class AddressBookServiceTest : WithAssertions {

    private val janeDoe = PrivateIndividual(FullName(FirstName("Jane"), LastName("Doe")))
    private val france = Country("France")
    private val homeTown = Town(PostalCode("75011"), City("Paris"))
    private val homeLocation = Location(StreetAddress(Street("12 rue des Lilas")), Locality(homeTown, france))
    private val officeStreet = Street("3 place du Marché")
    private val officeTown = Town(PostalCode("69001"), City("Lyon"))
    private val officeLocation = Location(StreetAddress(officeStreet), Locality(officeTown, france))
    private val home = PostalAddress(janeDoe, homeLocation)
    private val office = PostalAddress(janeDoe, officeLocation)
    private val userId = UserId.ValidUserId("user-1")
    private val email = Email("jane@example.com")
    private val definedUser = User.DefinedUser(userId, janeDoe.fullName, email, PlainPassword("pwd"))
    private val userRepository = InMemoryUserRepository().apply {
        save(definedUser.withPostalAddresses(PostalAddresses(home, office)))
    }
    private val addressUseCases = AddressUseCases(
        ChangePostalAddressUseCase(userRepository),
        RemovePostalAddressUseCase(userRepository)
    )
    private val addressBookService = AddressBookService(ReadAccountUseCase(userRepository), addressUseCases)

    private val first = UserAddress(userId, AddressPosition(0))

    @Test
    fun `a user should change one of their addresses, at the same place`() {
        // Arrange
        val acme = PostalAddress(LegalEntity(Company(CompanyName("Acme"))), homeLocation)
        // Act
        val addresses = addressBookService.change(AddressChange(first, acme))
        // Assert
        assertThat(addresses).isEqualTo(PostalAddresses(acme, office))
    }

    @Test
    fun `an address should not be changed into an incomplete one`() {
        // Arrange
        val nobody = PrivateIndividual(FullName(FirstName(""), LastName("")))
        val incompleteAddress = PostalAddress(nobody, homeLocation)
        // Act
        val thrown = catchThrowable { addressBookService.change(AddressChange(first, incompleteAddress)) }
        // Assert
        assertThat(thrown).isInstanceOf(IncompleteAddressException::class.java)
        assertThat(addressBookService.getAddresses(userId)).isEqualTo(PostalAddresses(home, office))
    }

    @Test
    fun `a user should remove one of their addresses`() {
        // Act
        val addresses = addressBookService.remove(first)
        // Assert
        assertThat(addresses).isEqualTo(PostalAddresses(office))
    }

    @Test
    fun `an address that does not exist should not be found`() {
        // Arrange
        val third = UserAddress(userId, AddressPosition(2))
        // Act
        val thrown = catchThrowable { addressBookService.remove(third) }
        // Assert
        assertThat(thrown).isInstanceOf(AddressNotFoundException::class.java)
    }
}
