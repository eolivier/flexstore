package org.flexstore.infra.spring.adapter.jpa

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.user.*
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

    private val home = PostalAddress(Street("12 rue des Lilas"), AddressComplement("Bâtiment B"), PostalCode("75011"), City("Paris"), Country("France"))
    private val office = PostalAddress(Street("3 place du Marché"), null, PostalCode("69001"), City("Lyon"), Country("France"))
    private val holidays = PostalAddress(Street("1 chemin des Vignes"), null, PostalCode("33000"), City("Bordeaux"), Country("France"))

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
        val officeRow = jpaUserRepository.findById(jane.id.value).orElseThrow().addresses.single { it.city == "Lyon" }
        // Act
        userRepository.save(jane.copy(postalAddresses = PostalAddresses(office, holidays)))
        // Assert
        val rows = jpaUserRepository.findById(jane.id.value).orElseThrow().addresses
        assertThat(storedAddresses()).isEqualTo(PostalAddresses(office, holidays))
        assertThat(rows.single { it.city == "Lyon" }.id).isEqualTo(officeRow.id)
        assertThat(rows.map { it.city }).doesNotContain("Paris")
    }
}
