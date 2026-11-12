package org.flexstore.domain.settings

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.settings.usecase.*
import org.flexstore.domain.user.*
import org.flexstore.infra.repository.InMemoryShopSettingsRepository
import org.junit.jupiter.api.Test
import org.flexstore.domain.user.administration.testAdministrator
import org.flexstore.domain.user.administration.testCustomer
import org.flexstore.domain.user.administration.unknownUser
import org.flexstore.domain.user.administration.usersOfTheAdministration

class ShopSettingsAdministrationServiceTest : WithAssertions {

    private val repository = InMemoryShopSettingsRepository()
    private val userRepository = usersOfTheAdministration()
    private val readAdministeredShopSettingsUseCase =
        ReadAdministeredShopSettingsUseCase(repository, userRepository)
    private val updateShopSettingsUseCase = UpdateShopSettingsUseCase(repository, userRepository)
    private val administration =
        ShopSettingsAdministrationService(readAdministeredShopSettingsUseCase, updateShopSettingsUseCase)
    private val administrator = testAdministrator.id

    @Test
    fun `the order confirmation email should be sent by default`() {
        // Act
        val settings = administration.getSettings(administrator)
        // Assert
        assertThat(settings.sendsOrderConfirmationEmail()).isTrue()
    }

    @Test
    fun `should disable the order confirmation email`() {
        // Act
        val savedSettings = administration.disableOrderConfirmationEmail(administrator)
        // Assert
        assertThat(savedSettings.sendsOrderConfirmationEmail()).isFalse()
    }

    @Test
    fun `should enable the order confirmation email again`() {
        // Arrange
        administration.disableOrderConfirmationEmail(administrator)
        // Act
        val savedSettings = administration.enableOrderConfirmationEmail(administrator)
        // Assert
        assertThat(savedSettings.sendsOrderConfirmationEmail()).isTrue()
    }

    @Test
    fun `the abandoned cart reminder should be off by default`() {
        // Act
        val settings = administration.getSettings(administrator)
        // Assert
        assertThat(settings.sendsAbandonedCartReminder()).isFalse()
    }

    @Test
    fun `should enable the abandoned cart reminder`() {
        // Act
        val savedSettings = administration.enableAbandonedCartReminder(administrator)
        // Assert
        assertThat(savedSettings.sendsAbandonedCartReminder()).isTrue()
    }

    @Test
    fun `should disable the abandoned cart reminder again`() {
        // Arrange
        administration.enableAbandonedCartReminder(administrator)
        // Act
        val savedSettings = administration.disableAbandonedCartReminder(administrator)
        // Assert
        assertThat(savedSettings.sendsAbandonedCartReminder()).isFalse()
    }

    @Test
    fun `a customer should not change the abandoned cart reminder`() {
        // Act
        val thrown = catchThrowable { administration.enableAbandonedCartReminder(testCustomer.id) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ShopSettingsAdministrationForbiddenException::class.java)
    }

    @Test
    fun `a customer should not change the settings of the shop`() {
        // Arrange
        val settingsBefore = repository.load()
        val settings = settingsBefore.withOrderConfirmationEmailDisabled()
        val update = ShopSettingsUpdate(testCustomer.id, settings)
        // Act
        val thrown = catchThrowable { updateShopSettingsUseCase.unfold(update) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ShopSettingsAdministrationForbiddenException::class.java)
        assertThat(repository.load()).isEqualTo(settingsBefore)
    }

    @Test
    fun `a customer should not read the settings of the shop`() {
        // Act
        val thrown = catchThrowable { administration.getSettings(testCustomer.id) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ShopSettingsAdministrationForbiddenException::class.java)
    }

    @Test
    fun `an unknown user should not change the settings of the shop`() {
        // Act
        val thrown = catchThrowable { administration.changePaymentDelay(unknownUser, PaymentDelay(30)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ShopSettingsAdministrationForbiddenException::class.java)
    }
}
