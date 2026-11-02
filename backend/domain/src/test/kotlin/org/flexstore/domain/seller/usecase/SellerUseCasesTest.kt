package org.flexstore.domain.seller.usecase

import org.flexstore.domain.seller.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemorySellerRepository
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test

class SellerUseCasesTest : WithAssertions {

    private val sellerRepository = InMemorySellerRepository(testSeller)
    private val readCurrentSellerUseCase = ReadCurrentSellerUseCase(sellerRepository)
    private val sellerUseCases = SellerUseCases(readCurrentSellerUseCase, UpdateSellerUseCase(sellerRepository))
    private val contact = testSellerDetail.contact
    private val identifier = testSellerDetail.identifier
    private val reach = testSellerDetail.reach

    @Test
    fun `an administrator should read the current seller`() {
        // Act
        val currentSeller = sellerUseCases.currentSeller()
        // Assert
        assertThat(currentSeller).isEqualTo(testSeller)
    }

    @Test
    fun `the edited seller should become the current seller, as a new version`() {
        // Arrange
        val renamedContact = SellerContact(SellerName("Flexstore Group"), contact.address)
        val sellerDetail = SellerDetail(renamedContact, identifier, reach)
        // Act
        val updatedSeller = sellerUseCases.update(sellerDetail)
        // Assert
        assertThat(updatedSeller.detail).isEqualTo(sellerDetail)
        assertThat(updatedSeller.id).isNotEqualTo(testSeller.id)
        assertThat(sellerRepository.findBy(testSeller.id)).isEqualTo(testSeller)
    }

    @ParameterizedTest(name = "a name \"{0}\" is refused")
    @ValueSource(strings = ["", "   "])
    fun `a seller without name should be refused`(name: String) {
        // Arrange
        val sellerDetail = SellerDetail(SellerContact(SellerName(name), contact.address), identifier, reach)
        // Act
        val thrown = catchThrowable { sellerUseCases.update(sellerDetail) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSellerException::class.java)
        assertThat(sellerRepository.current()).isEqualTo(testSeller)
    }

    @Test
    fun `a seller without address should be refused`() {
        // Arrange
        val sellerDetail = SellerDetail(SellerContact(contact.name, SellerAddress(" ")), identifier, reach)
        // Act
        val thrown = catchThrowable { sellerUseCases.update(sellerDetail) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSellerException::class.java)
    }

    @ParameterizedTest(name = "the SIRET \"{0}\" is accepted")
    @ValueSource(strings = ["12345678900012", "123 456 789 00012"])
    fun `a SIRET of 14 digits should be accepted, grouped by spaces or not`(siret: String) {
        // Arrange
        val sellerDetail = SellerDetail(contact, SellerIdentifier(Siret(siret), identifier.vatNumber), reach)
        // Act
        val updatedSeller = sellerUseCases.update(sellerDetail)
        // Assert
        assertThat(updatedSeller.detail.identifier.siret).isEqualTo(Siret(siret))
    }

    @ParameterizedTest(name = "the SIRET \"{0}\" is refused")
    @ValueSource(strings = ["", "1234567890001", "123456789000123", "1234567890001A"])
    fun `a SIRET that is not 14 digits should be refused`(siret: String) {
        // Arrange
        val sellerDetail = SellerDetail(contact, SellerIdentifier(Siret(siret), identifier.vatNumber), reach)
        // Act
        val thrown = catchThrowable { sellerUseCases.update(sellerDetail) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSellerException::class.java)
    }

    @ParameterizedTest(name = "the VAT number \"{0}\" is accepted")
    @ValueSource(strings = ["FR12345678901", "fr 12 345678901", "DE123456789"])
    fun `a VAT number of a country then 2 to 13 digits or letters should be accepted`(vatNumber: String) {
        // Arrange
        val sellerIdentifier = SellerIdentifier(identifier.siret, SellerVatNumber(vatNumber))
        val sellerDetail = SellerDetail(contact, sellerIdentifier, reach)
        // Act
        val updatedSeller = sellerUseCases.update(sellerDetail)
        // Assert
        assertThat(updatedSeller.detail.identifier.vatNumber).isEqualTo(SellerVatNumber(vatNumber))
    }

    @ParameterizedTest(name = "the VAT number \"{0}\" is refused")
    @ValueSource(strings = ["", "12345678901", "FR1", "FR12345678901234", "FR-1234567"])
    fun `a VAT number that is not well formed should be refused`(vatNumber: String) {
        // Arrange
        val sellerIdentifier = SellerIdentifier(identifier.siret, SellerVatNumber(vatNumber))
        val sellerDetail = SellerDetail(contact, sellerIdentifier, reach)
        // Act
        val thrown = catchThrowable { sellerUseCases.update(sellerDetail) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSellerException::class.java)
    }

    @ParameterizedTest(name = "the phone \"{0}\", the email \"{1}\" and the website \"{2}\" are refused")
    @CsvSource(
        "'', contact@flexstore.fr, www.flexstore.fr",
        "abc, contact@flexstore.fr, www.flexstore.fr",
        "01 23 45 67 89, '', www.flexstore.fr",
        "01 23 45 67 89, contact.flexstore.fr, www.flexstore.fr",
        "01 23 45 67 89, contact@flexstore.fr, ''",
        "01 23 45 67 89, contact@flexstore.fr, flexstore"
    )
    fun `a seller without a valid phone number, email and website should be refused`(
        phone: String, email: String, website: String
    ) {
        // Arrange
        val sellerReach = SellerReach(SellerPhone(phone), SellerEmail(email), SellerWebsite(website))
        // Act
        val thrown = catchThrowable { sellerUseCases.update(testSellerDetail.copy(reach = sellerReach)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSellerException::class.java)
    }

    @ParameterizedTest(name = "the phone \"{0}\" and the website \"{1}\" are accepted")
    @CsvSource("+33 1 23 45 67 89, https://www.flexstore.fr", "01.23.45.67.89, flexstore.fr/boutique")
    fun `a seller with a phone number, an email and a website should be saved`(phone: String, website: String) {
        // Arrange
        val sellerReach = SellerReach(SellerPhone(phone), SellerEmail("contact@flexstore.fr"), SellerWebsite(website))
        // Act
        val updatedSeller = sellerUseCases.update(testSellerDetail.copy(reach = sellerReach))
        // Assert
        assertThat(updatedSeller.detail.reach).isEqualTo(sellerReach)
    }
}
