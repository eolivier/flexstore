package org.flexstore.domain.settings

import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryShopSettingsRepository
import org.junit.jupiter.api.Test

class ShopSettingsAdministrationTest : WithAssertions {

    private val repository = InMemoryShopSettingsRepository()
    private val administration = ShopSettingsAdministration(ReadShopSettingsUseCase(repository), UpdateShopSettingsUseCase(repository))

    @Test
    fun `the order confirmation email should be sent by default`() {
        // Act
        val settings = administration.getSettings()
        // Assert
        assertThat(settings.sendsOrderConfirmationEmail()).isTrue()
    }

    @Test
    fun `should disable the order confirmation email`() {
        // Act
        val savedSettings = administration.disableOrderConfirmationEmail()
        // Assert
        assertThat(savedSettings.sendsOrderConfirmationEmail()).isFalse()
    }

    @Test
    fun `should enable the order confirmation email again`() {
        // Arrange
        administration.disableOrderConfirmationEmail()
        // Act
        val savedSettings = administration.enableOrderConfirmationEmail()
        // Assert
        assertThat(savedSettings.sendsOrderConfirmationEmail()).isTrue()
    }
}
