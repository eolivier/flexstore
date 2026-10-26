package org.flexstore.domain.settings

import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import java.time.Duration

enum class Activation { ENABLED, DISABLED }

// The time a customer has to pay an order once placed, in minutes: its units are reserved meanwhile.
// From 1 minute to 1 day
data class PaymentDelay(val minutes: Int) {

    fun isValid() = minutes in 1..MAX_MINUTES

    fun isInvalid() = !isValid()

    fun asDuration(): Duration = Duration.ofMinutes(minutes.toLong())

    companion object {
        const val MAX_MINUTES = 24 * 60

        fun byDefault() = PaymentDelay(10)
    }
}

// The settings of the shop, chosen by the administrators
data class ShopSettings(val orderConfirmationEmail: Activation, val paymentDelay: PaymentDelay) {

    constructor(orderConfirmationEmail: Activation) : this(orderConfirmationEmail, PaymentDelay.byDefault())

    fun sendsOrderConfirmationEmail() = orderConfirmationEmail == Activation.ENABLED

    fun withOrderConfirmationEmailEnabled() = copy(orderConfirmationEmail = Activation.ENABLED)

    fun withOrderConfirmationEmailDisabled() = copy(orderConfirmationEmail = Activation.DISABLED)

    fun withPaymentDelay(paymentDelay: PaymentDelay) = copy(paymentDelay = paymentDelay)

    companion object {
        fun byDefault() = ShopSettings(orderConfirmationEmail = Activation.ENABLED)
    }
}

data class InvalidPaymentDelayException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ShopSettingsUpdateFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
