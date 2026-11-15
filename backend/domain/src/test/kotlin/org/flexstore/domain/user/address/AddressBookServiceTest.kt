package org.flexstore.domain.user.address

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.model.*
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.flexstore.domain.user.address.usecase.AddPostalAddressUseCase
import org.flexstore.domain.user.address.usecase.ChangePostalAddressUseCase
import org.flexstore.domain.user.address.usecase.ListPostalAddressesUseCase
import org.flexstore.domain.user.address.usecase.RemovePostalAddressUseCase

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
    private val actorResolutionService = ActorResolutionService(userRepository)
    private val customer = actorResolutionService.actorOf(userId)
    private val addressUseCases = AddressUseCases(
        ListPostalAddressesUseCase(userRepository),
        AddPostalAddressUseCase(userRepository),
        ChangePostalAddressUseCase(userRepository),
        RemovePostalAddressUseCase(userRepository)
    )
    private val addressBookService = AddressBookService(addressUseCases)

    private val first = UserAddress(customer, AddressPosition(0))

    @Test
    fun `a user should add a new address, at the end of their addresses`() {
        // Arrange
        val workshopLocation = Location(StreetAddress(Street("5 rue de l'Atelier")), Locality(homeTown, france))
        val workshop = PostalAddress(janeDoe, workshopLocation)
        // Act
        val addresses = addressBookService.add(AddressAddition(customer, workshop))
        // Assert
        assertThat(addresses).isEqualTo(PostalAddresses(home, office, workshop))
    }

    @Test
    fun `an incomplete address should not be added`() {
        // Arrange
        val nobody = PrivateIndividual(FullName(FirstName(""), LastName("")))
        val incompleteAddress = PostalAddress(nobody, homeLocation)
        // Act
        val thrown = catchThrowable { addressBookService.add(AddressAddition(customer, incompleteAddress)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(IncompleteAddressException::class.java)
        assertThat(addressBookService.getAddresses(customer)).isEqualTo(PostalAddresses(home, office))
    }

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
        assertThat(thrown).hasCauseInstanceOf(IncompleteAddressException::class.java)
        assertThat(addressBookService.getAddresses(customer)).isEqualTo(PostalAddresses(home, office))
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
        val third = UserAddress(customer, AddressPosition(2))
        // Act
        val thrown = catchThrowable { addressBookService.remove(third) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AddressNotFoundException::class.java)
    }

    @Test
    fun `an unknown user should not read their addresses`() {
        // Arrange
        val unknownUser = actorResolutionService.actorOf(unknownCustomerId)
        // Act
        val thrown = catchThrowable { addressBookService.getAddresses(unknownUser) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `an unknown user should not add an address`() {
        // Arrange
        val unknownUser = actorResolutionService.actorOf(unknownCustomerId)
        // Act
        val thrown = catchThrowable { addressBookService.add(AddressAddition(unknownUser, home)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a user whose account is refused should not read nor change their addresses`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status).withPostalAddresses(PostalAddresses(home)))
        val blockedCustomer = actorResolutionService.actorOf(blockedCustomerId)
        val blockedAddress = UserAddress(blockedCustomer, AddressPosition(0))
        // Act
        val thrownByReading = catchThrowable { addressBookService.getAddresses(blockedCustomer) }
        val thrownByAdding = catchThrowable { addressBookService.add(AddressAddition(blockedCustomer, office)) }
        val thrownByChanging = catchThrowable { addressBookService.change(AddressChange(blockedAddress, office)) }
        val thrownByRemoving = catchThrowable { addressBookService.remove(blockedAddress) }
        // Assert
        listOf(thrownByReading, thrownByAdding, thrownByChanging, thrownByRemoving).forEach { thrown ->
            assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
        }
    }
}
