package org.flexstore.domain.settings

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.settings.usecase.*
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.infra.repository.InMemoryShopSettingsRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class AbandonedCartDelayTest : WithAssertions {

    private val shopSettingsRepository = InMemoryShopSettingsRepository()
    private val service = ShopSettingsAdministrationService(
        ReadAdministeredShopSettingsUseCase(shopSettingsRepository),
        UpdateShopSettingsUseCase(shopSettingsRepository)
    )

    @ParameterizedTest(name = "a delay of {0} hours is saved: {1}")
    @CsvSource("0, false", "1, true", "24, true", "720, true", "721, false")
    fun `an abandoned cart delay should be from 1 hour to 30 days`(hours: Int, saved: Boolean) {
        // Act
        val thrown = catchThrowable {
            service.changeAbandonedCartDelay(testAdministratorActor, AbandonedCartDelay(hours))
        }
        // Assert
        assertThat(thrown == null).isEqualTo(saved)
        val storedDelay = shopSettingsRepository.load().abandonedCartDelay
        assertThat(storedDelay == AbandonedCartDelay(hours)).isEqualTo(saved)
    }

    @Test
    fun `an invalid abandoned cart delay should be refused with its own error`() {
        // Act
        val thrown = catchThrowable {
            service.changeAbandonedCartDelay(testAdministratorActor, AbandonedCartDelay(0))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidAbandonedCartDelayException::class.java)
    }

    @Test
    fun `the abandoned cart delay should be one day by default`() {
        // Act
        val settings = service.getSettings(testAdministratorActor)
        // Assert
        assertThat(settings.abandonedCartDelay).isEqualTo(AbandonedCartDelay(24))
    }
}
