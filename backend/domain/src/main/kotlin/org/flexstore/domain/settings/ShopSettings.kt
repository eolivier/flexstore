package org.flexstore.domain.settings

import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage

enum class Activation { ENABLED, DISABLED }

// The settings of the shop, chosen by the administrators
data class ShopSettings(val orderConfirmationEmail: Activation) {

    fun sendsOrderConfirmationEmail() = orderConfirmationEmail == Activation.ENABLED

    fun withOrderConfirmationEmailEnabled() = copy(orderConfirmationEmail = Activation.ENABLED)

    fun withOrderConfirmationEmailDisabled() = copy(orderConfirmationEmail = Activation.DISABLED)

    companion object {
        fun byDefault() = ShopSettings(orderConfirmationEmail = Activation.ENABLED)
    }
}

data class ShopSettingsUpdateFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
