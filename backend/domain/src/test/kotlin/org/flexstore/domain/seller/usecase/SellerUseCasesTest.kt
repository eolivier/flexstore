package org.flexstore.domain.seller.usecase

import org.flexstore.domain.seller.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemorySellerRepository
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test

class SellerUseCasesTest : WithAssertions {

    private val sellerRepository = InMemorySellerRepository(testSeller)
    private val readCurrentSellerUseCase = ReadCurrentSellerUseCase(sellerRepository)
    private val sellerUseCases = SellerUseCases(readCurrentSellerUseCase, UpdateSellerUseCase(sellerRepository))
    private val contact = testSellerDetails.contact
    private val identifiers = testSellerDetails.identifiers

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
        val sellerDetails = SellerDetails(renamedContact, identifiers)
        // Act
        val updatedSeller = sellerUseCases.update(sellerDetails)
        // Assert
        assertThat(updatedSeller.details).isEqualTo(sellerDetails)
        assertThat(updatedSeller.id).isNotEqualTo(testSeller.id)
        assertThat(sellerRepository.findBy(testSeller.id)).isEqualTo(testSeller)
    }

    @ParameterizedTest(name = "a name \"{0}\" is refused")
    @ValueSource(strings = ["", "   "])
    fun `a seller without name should be refused`(name: String) {
        // Arrange
        val sellerDetails = SellerDetails(SellerContact(SellerName(name), contact.address), identifiers)
        // Act
        val thrown = catchThrowable { sellerUseCases.update(sellerDetails) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSellerException::class.java)
        assertThat(sellerRepository.current()).isEqualTo(testSeller)
    }

    @Test
    fun `a seller without address should be refused`() {
        // Arrange
        val sellerDetails = SellerDetails(SellerContact(contact.name, SellerAddress(" ")), identifiers)
        // Act
        val thrown = catchThrowable { sellerUseCases.update(sellerDetails) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSellerException::class.java)
    }

    @ParameterizedTest(name = "the SIRET \"{0}\" is accepted")
    @ValueSource(strings = ["12345678900012", "123 456 789 00012"])
    fun `a SIRET of 14 digits should be accepted, grouped by spaces or not`(siret: String) {
        // Arrange
        val sellerDetails = SellerDetails(contact, SellerIdentifiers(Siret(siret), identifiers.vatNumber))
        // Act
        val updatedSeller = sellerUseCases.update(sellerDetails)
        // Assert
        assertThat(updatedSeller.details.identifiers.siret).isEqualTo(Siret(siret))
    }

    @ParameterizedTest(name = "the SIRET \"{0}\" is refused")
    @ValueSource(strings = ["", "1234567890001", "123456789000123", "1234567890001A"])
    fun `a SIRET that is not 14 digits should be refused`(siret: String) {
        // Arrange
        val sellerDetails = SellerDetails(contact, SellerIdentifiers(Siret(siret), identifiers.vatNumber))
        // Act
        val thrown = catchThrowable { sellerUseCases.update(sellerDetails) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSellerException::class.java)
    }

    @ParameterizedTest(name = "the VAT number \"{0}\" is accepted")
    @ValueSource(strings = ["FR12345678901", "fr 12 345678901", "DE123456789"])
    fun `a VAT number of a country then 2 to 13 digits or letters should be accepted`(vatNumber: String) {
        // Arrange
        val sellerIdentifiers = SellerIdentifiers(identifiers.siret, SellerVatNumber(vatNumber))
        val sellerDetails = SellerDetails(contact, sellerIdentifiers)
        // Act
        val updatedSeller = sellerUseCases.update(sellerDetails)
        // Assert
        assertThat(updatedSeller.details.identifiers.vatNumber).isEqualTo(SellerVatNumber(vatNumber))
    }

    @ParameterizedTest(name = "the VAT number \"{0}\" is refused")
    @ValueSource(strings = ["", "12345678901", "FR1", "FR12345678901234", "FR-1234567"])
    fun `a VAT number that is not well formed should be refused`(vatNumber: String) {
        // Arrange
        val sellerIdentifiers = SellerIdentifiers(identifiers.siret, SellerVatNumber(vatNumber))
        val sellerDetails = SellerDetails(contact, sellerIdentifiers)
        // Act
        val thrown = catchThrowable { sellerUseCases.update(sellerDetails) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSellerException::class.java)
    }
}
