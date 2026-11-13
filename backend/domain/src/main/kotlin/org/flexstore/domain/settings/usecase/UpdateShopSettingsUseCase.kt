package org.flexstore.domain.settings.usecase

import org.flexstore.domain.settings.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator changes the settings of the shop: a payment delay from 1 minute to a day, a threshold of free
// shipping of 0 or more
class UpdateShopSettingsUseCase(
    private val shopSettingsRepository: ShopSettingsRepository
) : UseCase<ShopSettingsUpdate> {

    override fun getPreConditions(): PreConditions<ShopSettingsUpdate> = PreConditions(
        editorIsAdministratorCondition(), paymentDelayIsValidCondition(),
        freeShippingThresholdIsValidCondition(), abandonedCartDelayIsValidCondition()
    )

    override fun getNominalScenario(): NominalScenario<ShopSettingsUpdate> =
        NominalScenario(Step { update -> shopSettingsRepository.save(update.settings) })

    override fun getPostConditions(): PostConditions<ShopSettingsUpdate> =
        PostConditions(settingsAreSavedCondition())

    // The editor, resolved before the use case runs, must be an Admin
    private fun editorIsAdministratorCondition() = PreCondition<ShopSettingsUpdate> { update ->
        if (update.editor.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can change the settings of the shop.")
            throw ShopSettingsAdministrationForbiddenException(message)
        }
    }

    private fun paymentDelayIsValidCondition() = PreCondition<ShopSettingsUpdate> { (_, settings) ->
        if (settings.paymentDelay.isInvalid()) {
            val message = ErrorMessage("The payment delay must be from 1 to ${PaymentDelay.MAX_MINUTES} minutes.")
            throw InvalidPaymentDelayException(message)
        }
    }

    private fun freeShippingThresholdIsValidCondition() = PreCondition<ShopSettingsUpdate> { (_, settings) ->
        if (settings.freeShippingThreshold.isInvalid()) {
            val message = ErrorMessage("The threshold of free shipping must be 0 or more.")
            throw InvalidFreeShippingThresholdException(message)
        }
    }

    private fun abandonedCartDelayIsValidCondition() = PreCondition<ShopSettingsUpdate> { (_, settings) ->
        if (settings.abandonedCartDelay.isInvalid()) {
            val maxHours = AbandonedCartDelay.MAX_HOURS
            val message = ErrorMessage("The abandoned cart delay must be from 1 to $maxHours hours.")
            throw InvalidAbandonedCartDelayException(message)
        }
    }

    private fun settingsAreSavedCondition() = PostCondition<ShopSettingsUpdate> { (_, settings) ->
        if (shopSettingsRepository.load() != settings) {
            throw ShopSettingsUpdateFailed(ErrorMessage("The shop settings were not saved."))
        }
    }
}
