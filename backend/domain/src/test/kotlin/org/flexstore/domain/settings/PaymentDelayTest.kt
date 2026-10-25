package org.flexstore.domain.settings

import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryShopSettingsRepository
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PaymentDelayTest : WithAssertions {

    @ParameterizedTest
    @CsvSource("0, false", "1, true", "10, true", "1440, true", "1441, false")
    fun `a payment delay should be from 1 minute to 1 day`(minutes: Int, saved: Boolean) {
        // Arrange
        val shopSettingsRepository = InMemoryShopSettingsRepository()
        val service = ShopSettingsAdministrationService(
            ReadShopSettingsUseCase(shopSettingsRepository), UpdateShopSettingsUseCase(shopSettingsRepository)
        )
        // Act
        val thrown = catchThrowable { service.changePaymentDelay(PaymentDelay(minutes)) }
        // Assert
        assertThat(thrown == null).isEqualTo(saved)
        assertThat(shopSettingsRepository.load().paymentDelay == PaymentDelay(minutes)).isEqualTo(saved)
    }
}
