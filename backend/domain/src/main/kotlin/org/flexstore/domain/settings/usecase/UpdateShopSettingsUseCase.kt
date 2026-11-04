package org.flexstore.domain.settings.usecase

import org.flexstore.domain.settings.*
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator changes the settings of the shop: a payment delay from 1 minute to a day, a threshold of free
// shipping of 0 or more
class UpdateShopSettingsUseCase(
    private val shopSettingsRepository: ShopSettingsRepository,
    private val userRepository: UserRepository
) : UseCase<ShopSettingsUpdate> {

    override fun getPreConditions(): PreConditions<ShopSettingsUpdate> = PreConditions(
        editorIsAdministratorCondition(), paymentDelayIsValidCondition(), freeShippingThresholdIsValidCondition()
    )

    override fun getNominalScenario(): NominalScenario<ShopSettingsUpdate> =
        NominalScenario(Step { update -> shopSettingsRepository.save(update.settings) })

    override fun getPostConditions(): PostConditions<ShopSettingsUpdate> =
        PostConditions(settingsAreSavedCondition())

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<ShopSettingsUpdate> { update ->
        if (userRepository.isNotAdministrator(update.editor)) {
            throw ShopSettingsAdministrationForbiddenException(
                ErrorMessage("Only an administrator can change the settings of the shop.")
            )
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

    private fun settingsAreSavedCondition() = PostCondition<ShopSettingsUpdate> { (_, settings) ->
        if (shopSettingsRepository.load() != settings) {
            throw ShopSettingsUpdateFailed(ErrorMessage("The shop settings were not saved."))
        }
    }
}
