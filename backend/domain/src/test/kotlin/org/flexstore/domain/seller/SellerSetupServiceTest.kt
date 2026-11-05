package org.flexstore.domain.seller

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.seller.usecase.*
import org.flexstore.domain.settings.ShopSettingsAdministrationService
import org.flexstore.domain.settings.usecase.ReadAdministeredShopSettingsUseCase
import org.flexstore.domain.settings.usecase.UpdateShopSettingsUseCase
import org.flexstore.domain.user.*
import org.flexstore.infra.repository.InMemorySellerRepository
import org.flexstore.infra.repository.InMemoryShopSettingsRepository
import org.junit.jupiter.api.Test

class SellerSetupServiceTest : WithAssertions {

    private val sellerRepository = InMemorySellerRepository(testSeller)
    private val shopSettingsRepository = InMemoryShopSettingsRepository()
    private val userRepository = usersOfTheAdministration()
    private val sellerUseCases = SellerUseCases(
        ReadCurrentSellerUseCase(sellerRepository, userRepository),
        UpdateSellerUseCase(sellerRepository, userRepository),
        ReadSocialNetworksUseCase(sellerRepository)
    )
    private val shopSettingsAdministrationService = ShopSettingsAdministrationService(
        ReadAdministeredShopSettingsUseCase(shopSettingsRepository, userRepository),
        UpdateShopSettingsUseCase(shopSettingsRepository, userRepository)
    )
    private val sellerSetupService = SellerSetupService(sellerUseCases, shopSettingsAdministrationService)
    private val administrator = testAdministrator.id
    private val invalidSellerDetail = testSellerDetail.copy(
        contact = SellerContact(SellerName(""), testSellerDetail.contact.address)
    )

    @Test
    fun `the seller should not be set up by default`() {
        // Act
        val settings = shopSettingsRepository.load()
        // Assert
        assertThat(settings.isSellerNotSetUp()).isTrue()
    }

    @Test
    fun `completing the setup should save the seller as the current one`() {
        // Act
        val seller = sellerSetupService.complete(administrator, testSellerDetail)
        // Assert
        assertThat(seller.detail).isEqualTo(testSellerDetail)
        assertThat(sellerRepository.current()).isEqualTo(seller)
    }

    @Test
    fun `completing the setup should turn the seller setup of the settings on`() {
        // Act
        sellerSetupService.complete(administrator, testSellerDetail)
        // Assert
        assertThat(shopSettingsRepository.load().isSellerSetUp()).isTrue()
    }

    @Test
    fun `completing the setup should keep the other settings`() {
        // Arrange
        val settingsBefore = shopSettingsRepository.load()
        // Act
        sellerSetupService.complete(administrator, testSellerDetail)
        // Assert
        assertThat(shopSettingsRepository.load()).isEqualTo(settingsBefore.withSellerSetUp())
    }

    @Test
    fun `a refused seller should leave the setup to be done`() {
        // Act
        val thrown = catchThrowable { sellerSetupService.complete(administrator, invalidSellerDetail) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSellerException::class.java)
        assertThat(shopSettingsRepository.load().isSellerNotSetUp()).isTrue()
    }

    @Test
    fun `a customer should not complete the setup`() {
        // Act
        val thrown = catchThrowable { sellerSetupService.complete(testCustomer.id, testSellerDetail) }
        // Assert
        assertThat(thrown).isInstanceOf(SellerAdministrationForbiddenException::class.java)
        assertThat(shopSettingsRepository.load().isSellerNotSetUp()).isTrue()
    }

    @Test
    fun `an unknown user should not complete the setup`() {
        // Act
        val thrown = catchThrowable { sellerSetupService.complete(unknownUser, testSellerDetail) }
        // Assert
        assertThat(thrown).isInstanceOf(SellerAdministrationForbiddenException::class.java)
        assertThat(shopSettingsRepository.load().isSellerNotSetUp()).isTrue()
    }

    @Test
    fun `the other settings changes should keep the seller setup`() {
        // Arrange
        sellerSetupService.complete(administrator, testSellerDetail)
        // Act
        val savedSettings = shopSettingsAdministrationService.disableOrderConfirmationEmail(administrator)
        // Assert
        assertThat(savedSettings.isSellerSetUp()).isTrue()
    }
}
