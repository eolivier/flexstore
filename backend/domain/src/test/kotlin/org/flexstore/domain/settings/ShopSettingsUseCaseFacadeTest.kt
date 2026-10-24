package org.flexstore.domain.settings

import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryShopSettingsRepository
import org.junit.jupiter.api.Test

class ShopSettingsUseCaseFacadeTest : WithAssertions {

    private val repository = InMemoryShopSettingsRepository()
    private val facade = ShopSettingsUseCaseFacade(repository, UpdateShopSettingsUseCase(repository))

    @Test
    fun `the order confirmation email should be sent by default`() {
        // Act
        val settings = facade.getSettings()
        // Assert
        assertThat(settings.sendsOrderConfirmationEmail()).isTrue()
    }

    @Test
    fun `should disable the order confirmation email`() {
        // Act
        val savedSettings = facade.disableOrderConfirmationEmail()
        // Assert
        assertThat(savedSettings.sendsOrderConfirmationEmail()).isFalse()
    }

    @Test
    fun `should enable the order confirmation email again`() {
        // Arrange
        facade.disableOrderConfirmationEmail()
        // Act
        val savedSettings = facade.enableOrderConfirmationEmail()
        // Assert
        assertThat(savedSettings.sendsOrderConfirmationEmail()).isTrue()
    }
}
