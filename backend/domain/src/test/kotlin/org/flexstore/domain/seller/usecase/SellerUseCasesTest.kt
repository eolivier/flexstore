package org.flexstore.domain.seller.usecase

import org.flexstore.domain.seller.*
import org.flexstore.domain.user.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemorySellerRepository
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.domain.actor.testCustomerActor
import org.flexstore.domain.actor.unknownUserActor

class SellerUseCasesTest : WithAssertions {

    private val sellerRepository = InMemorySellerRepository(testSeller)
    private val readCurrentSellerUseCase = ReadCurrentSellerUseCase(sellerRepository)
    private val updateSellerUseCase = UpdateSellerUseCase(sellerRepository)
    private val readSocialNetworksUseCase = ReadSocialNetworksUseCase(sellerRepository)
    private val sellerUseCases =
        SellerUseCases(readCurrentSellerUseCase, updateSellerUseCase, readSocialNetworksUseCase)
    private val administrator = testAdministratorActor
    private val contact = testSellerDetail.contact
    private val identifier = testSellerDetail.identifier

    @Test
    fun `an administrator should read the current seller`() {
        // Act
        val currentSeller = sellerUseCases.currentSeller(administrator)
        // Assert
        assertThat(currentSeller).isEqualTo(testSeller)
    }

    @Test
    fun `the edited seller should become the current seller, as a new version`() {
        // Arrange
        val renamedContact = SellerContact(SellerName("Flexstore Group"), contact.address)
        val sellerDetail = testSellerDetail.copy(contact = renamedContact)
        // Act
        val updatedSeller = sellerUseCases.update(administrator, sellerDetail)
        // Assert
        assertThat(updatedSeller.detail).isEqualTo(sellerDetail)
        assertThat(updatedSeller.id).isNotEqualTo(testSeller.id)
        assertThat(sellerRepository.findBy(testSeller.id)).isEqualTo(testSeller)
    }

    @ParameterizedTest(name = "a name \"{0}\" is refused")
    @ValueSource(strings = ["", "   "])
    fun `a seller without name should be refused`(name: String) {
        // Arrange
        val sellerDetail = testSellerDetail.copy(contact = SellerContact(SellerName(name), contact.address))
        // Act
        val thrown = catchThrowable { sellerUseCases.update(administrator, sellerDetail) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidSellerException::class.java)
        assertThat(sellerRepository.current()).isEqualTo(testSeller)
    }

    @Test
    fun `a seller without address should be refused`() {
        // Arrange
        val sellerDetail = testSellerDetail.copy(contact = SellerContact(contact.name, SellerAddress(" ")))
        // Act
        val thrown = catchThrowable { sellerUseCases.update(administrator, sellerDetail) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidSellerException::class.java)
    }

    @ParameterizedTest(name = "the SIRET \"{0}\" is accepted")
    @ValueSource(strings = ["12345678900012", "123 456 789 00012"])
    fun `a SIRET of 14 digits should be accepted, grouped by spaces or not`(siret: String) {
        // Arrange
        val sellerIdentifier = SellerIdentifier(Siret(siret), identifier.vatNumber)
        val sellerDetail = testSellerDetail.copy(identifier = sellerIdentifier)
        // Act
        val updatedSeller = sellerUseCases.update(administrator, sellerDetail)
        // Assert
        assertThat(updatedSeller.detail.identifier.siret).isEqualTo(Siret(siret))
    }

    @ParameterizedTest(name = "the SIRET \"{0}\" is refused")
    @ValueSource(strings = ["", "1234567890001", "123456789000123", "1234567890001A"])
    fun `a SIRET that is not 14 digits should be refused`(siret: String) {
        // Arrange
        val sellerIdentifier = SellerIdentifier(Siret(siret), identifier.vatNumber)
        val sellerDetail = testSellerDetail.copy(identifier = sellerIdentifier)
        // Act
        val thrown = catchThrowable { sellerUseCases.update(administrator, sellerDetail) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidSellerException::class.java)
    }

    @ParameterizedTest(name = "the VAT number \"{0}\" is accepted")
    @ValueSource(strings = ["FR12345678901", "fr 12 345678901", "DE123456789"])
    fun `a VAT number of a country then 2 to 13 digits or letters should be accepted`(vatNumber: String) {
        // Arrange
        val sellerIdentifier = SellerIdentifier(identifier.siret, SellerVatNumber(vatNumber))
        val sellerDetail = testSellerDetail.copy(identifier = sellerIdentifier)
        // Act
        val updatedSeller = sellerUseCases.update(administrator, sellerDetail)
        // Assert
        assertThat(updatedSeller.detail.identifier.vatNumber).isEqualTo(SellerVatNumber(vatNumber))
    }

    @ParameterizedTest(name = "the VAT number \"{0}\" is refused")
    @ValueSource(strings = ["", "12345678901", "FR1", "FR12345678901234", "FR-1234567"])
    fun `a VAT number that is not well formed should be refused`(vatNumber: String) {
        // Arrange
        val sellerIdentifier = SellerIdentifier(identifier.siret, SellerVatNumber(vatNumber))
        val sellerDetail = testSellerDetail.copy(identifier = sellerIdentifier)
        // Act
        val thrown = catchThrowable { sellerUseCases.update(administrator, sellerDetail) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidSellerException::class.java)
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
        val sellerDetail = testSellerDetail.copy(reach = sellerReach)
        // Act
        val thrown = catchThrowable { sellerUseCases.update(administrator, sellerDetail) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidSellerException::class.java)
    }

    @ParameterizedTest(name = "the phone \"{0}\" and the website \"{1}\" are accepted")
    @CsvSource("+33 1 23 45 67 89, https://www.flexstore.fr", "01.23.45.67.89, flexstore.fr/boutique")
    fun `a seller with a phone number, an email and a website should be saved`(phone: String, website: String) {
        // Arrange
        val sellerReach = SellerReach(SellerPhone(phone), SellerEmail("contact@flexstore.fr"), SellerWebsite(website))
        // Act
        val updatedSeller = sellerUseCases.update(administrator, testSellerDetail.copy(reach = sellerReach))
        // Assert
        assertThat(updatedSeller.detail.reach).isEqualTo(sellerReach)
    }

    @ParameterizedTest(name = "the legal form \"{0}\", the share capital \"{1}\" and the city \"{2}\" are refused")
    @CsvSource("'', 10 000, Paris", "SAS, '', Paris", "SAS, dix mille, Paris", "SAS, 10 000, ''")
    fun `a seller without its legal form, a share capital in euros and its register should be refused`(
        legalForm: String, shareCapital: String, registryCity: String
    ) {
        // Arrange
        val sellerLegalForm = SellerLegalForm(legalForm)
        val sellerIncorporation =
            SellerIncorporation(sellerLegalForm, ShareCapital(shareCapital), RegistryCity(registryCity))
        // Act
        val thrown = catchThrowable {
            sellerUseCases.update(administrator, testSellerDetail.copy(incorporation = sellerIncorporation))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidSellerException::class.java)
    }

    @ParameterizedTest(name = "the address \"{0}\" of a social network is refused")
    @ValueSource(strings = ["facebook", "https://", "face book.com/flexstore", "www."])
    fun `a social network that is not a web address should be refused`(address: String) {
        // Arrange
        val socialNetworks = SocialNetworks.of(mapOf(SocialNetwork.FACEBOOK to SocialNetworkUrl(address)))
        // Act
        val thrown = catchThrowable {
            sellerUseCases.update(administrator, testSellerDetail.copy(socialNetworks = socialNetworks))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidSellerException::class.java)
        assertThat(sellerRepository.current()).isEqualTo(testSeller)
    }

    @ParameterizedTest(name = "the address \"{0}\" of a social network is accepted")
    @ValueSource(strings = ["https://www.facebook.com/flexstore", "www.facebook.com/flexstore", ""])
    fun `a social network given as a web address, or not given, should be saved`(address: String) {
        // Arrange
        val socialNetworks = SocialNetworks.of(mapOf(SocialNetwork.FACEBOOK to SocialNetworkUrl(address)))
        // Act
        val updatedSeller =
            sellerUseCases.update(administrator, testSellerDetail.copy(socialNetworks = socialNetworks))
        // Assert
        assertThat(updatedSeller.detail.socialNetworks).isEqualTo(socialNetworks)
    }

    @Test
    fun `everyone should read the social networks of the current seller`() {
        // Arrange
        val instagramUrl = SocialNetworkUrl("https://www.instagram.com/flexstore")
        val socialNetworks = SocialNetworks.of(mapOf(SocialNetwork.INSTAGRAM to instagramUrl))
        sellerUseCases.update(administrator, testSellerDetail.copy(socialNetworks = socialNetworks))
        // Act
        val readSocialNetworks = sellerUseCases.socialNetworks()
        // Assert
        assertThat(readSocialNetworks.urlOf(SocialNetwork.INSTAGRAM)).isEqualTo(instagramUrl)
        assertThat(readSocialNetworks.urlOf(SocialNetwork.TWITCH).isNotGiven()).isTrue()
    }

    @Test
    fun `a customer should not change the seller`() {
        // Act
        val thrown = catchThrowable { sellerUseCases.update(testCustomerActor, testSellerDetail) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(SellerAdministrationForbiddenException::class.java)
        assertThat(sellerRepository.current()).isEqualTo(testSeller)
    }

    @Test
    fun `a customer should not read the seller`() {
        // Act
        val thrown = catchThrowable { sellerUseCases.currentSeller(testCustomerActor) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(SellerAdministrationForbiddenException::class.java)
    }

    @Test
    fun `an unknown user should not change the seller`() {
        // Act
        val thrown = catchThrowable { sellerUseCases.update(unknownUserActor, testSellerDetail) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(SellerAdministrationForbiddenException::class.java)
    }
}
