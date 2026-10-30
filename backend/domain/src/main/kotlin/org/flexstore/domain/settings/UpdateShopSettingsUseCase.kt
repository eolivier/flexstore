package org.flexstore.domain.settings

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

class UpdateShopSettingsUseCase(private val shopSettingsRepository: ShopSettingsRepository) :
    UseCase<ShopSettings> {

    override fun getPreConditions(): PreConditions<ShopSettings> =
        PreConditions(paymentDelayIsValidCondition(), freeShippingThresholdIsValidCondition())

    override fun getNominalScenario(): NominalScenario<ShopSettings> =
        NominalScenario(Step { settings -> shopSettingsRepository.save(settings) })

    override fun getPostConditions(): PostConditions<ShopSettings> = PostConditions(settingsAreSavedCondition())

    private fun paymentDelayIsValidCondition() = PreCondition<ShopSettings> { settings ->
        if (settings.paymentDelay.isInvalid()) {
            val message = ErrorMessage("The payment delay must be from 1 to ${PaymentDelay.MAX_MINUTES} minutes.")
            throw InvalidPaymentDelayException(message)
        }
    }

    private fun freeShippingThresholdIsValidCondition() = PreCondition<ShopSettings> { settings ->
        if (settings.freeShippingThreshold.isInvalid()) {
            val message = ErrorMessage("The threshold of free shipping must be 0 or more.")
            throw InvalidFreeShippingThresholdException(message)
        }
    }

    private fun settingsAreSavedCondition() = PostCondition<ShopSettings> { settings ->
        if (shopSettingsRepository.load() != settings) {
            throw ShopSettingsUpdateFailed(ErrorMessage("The shop settings were not saved."))
        }
    }
}
