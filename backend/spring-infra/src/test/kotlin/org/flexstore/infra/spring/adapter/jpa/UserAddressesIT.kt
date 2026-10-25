package org.flexstore.infra.spring.adapter.jpa

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class UserAddressesIT {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jpaUserRepository: JpaUserRepository

    private val janeDoe = PrivateIndividual(FullName(FirstName("Jane"), LastName("Doe")))
    private val acmeCompany = Company(CompanyName("Acme"), VatNumber("FR12345678901"))
    private val acme = LegalEntity(acmeCompany, FullName(FirstName("John"), LastName("Smith")))

    private val homeStreet = Street("12 rue des Lilas")
    private val homeComplement = AddressComplement("Bâtiment B")
    private val france = Country("France")
    private val homeTown = Town(PostalCode("75011"), City("Paris"))
    private val homeLocation = Location(StreetAddress(homeStreet, homeComplement), Locality(homeTown, france))
    private val officeStreet = Street("3 place du Marché")
    private val officeTown = Town(PostalCode("69001"), City("Lyon"))
    private val officeLocation = Location(StreetAddress(officeStreet), Locality(officeTown, france))
    private val holidaysStreet = Street("1 chemin des Vignes")
    private val holidaysTown = Town(PostalCode("33000"), City("Bordeaux"))
    private val holidaysLocation = Location(StreetAddress(holidaysStreet), Locality(holidaysTown, france))

    private val home = PostalAddress(janeDoe, homeLocation)
    private val office = PostalAddress(acme, officeLocation)
    private val holidays = PostalAddress(janeDoe, holidaysLocation)

    private val jane = User.DefinedUser(
        UserId.ValidUserId("addresses-it-jane"),
        FullName(FirstName("Jane"), LastName("Doe")),
        Email("addresses-it.jane@example.com"),
        PlainPassword("password123"),
        postalAddresses = PostalAddresses(home, office)
    )

    @AfterEach
    fun deleteJane() {
        jpaUserRepository.deleteById(jane.id.value)
    }

    private fun storedAddresses() = (userRepository.findById(jane.id) as User.DefinedUser).postalAddresses

    @Test
    fun `a user should keep several postal addresses, in their order`() {
        // Act
        userRepository.save(jane)
        // Assert
        assertThat(storedAddresses()).isEqualTo(PostalAddresses(home, office))
    }

    @Test
    fun `saving a user again should keep the rows of the addresses that did not change`() {
        // Arrange
        userRepository.save(jane)
        val storedJane = jpaUserRepository.findById(jane.id.value).orElseThrow()
        val officeRow = storedJane.addresses.single { it.address.city == "Lyon" }
        // Act
        userRepository.save(jane.copy(postalAddresses = PostalAddresses(office, holidays)))
        // Assert
        val rows = jpaUserRepository.findById(jane.id.value).orElseThrow().addresses
        assertThat(storedAddresses()).isEqualTo(PostalAddresses(office, holidays))
        assertThat(rows.single { it.address.city == "Lyon" }.address.id).isEqualTo(officeRow.address.id)
        assertThat(rows.map { it.address.city }).doesNotContain("Paris")
    }

    @Test
    fun `an address should keep its recipient, who may be a company`() {
        // Act
        userRepository.save(jane)
        // Assert
        val recipients = storedAddresses().asList().map { it.recipient }
        assertThat(recipients).containsExactly(janeDoe, acme)
    }
}
