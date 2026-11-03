package org.flexstore.domain.settings

import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal

import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import java.time.Duration
import org.flexstore.domain.user.model.UserId

enum class Activation { ENABLED, DISABLED }

// The time a customer has to pay an order once placed, in minutes: its units are reserved meanwhile.
// From 1 minute to 1 day
data class PaymentDelay(val minutes: Int) {

    fun isValid() = minutes in 1..MAX_MINUTES

    fun isInvalid() = !isValid()

    fun asDuration(): Duration = Duration.ofMinutes(minutes.toLong())

    companion object {
        const val MAX_MINUTES = 24 * 60
        private const val DEFAULT_MINUTES = 10

        fun byDefault() = PaymentDelay(DEFAULT_MINUTES)
    }
}

// The price of the items of an order from which its standard shipping is free (0: always free)
data class FreeShippingThreshold(val price: Price) {

    fun isReachedBy(itemsPrice: Price) = itemsPrice.isAtLeast(price)

    fun isValid() = price.amount.value.signum() >= 0

    fun isInvalid() = !isValid()

    companion object {
        private const val DEFAULT_EUROS = 50

        fun byDefault() = FreeShippingThreshold(Price(Amount(BigDecimal(DEFAULT_EUROS)), Currency.EUR))
    }
}

// The rules of the shop about the orders: how long a customer has to pay one, and from which price its standard
// shipping is free
data class OrderRules(val paymentDelay: PaymentDelay, val freeShippingThreshold: FreeShippingThreshold) {
    companion object {
        fun byDefault() = OrderRules(PaymentDelay.byDefault(), FreeShippingThreshold.byDefault())
    }
}

// The settings of the shop, chosen by the administrators
data class ShopSettings(val orderConfirmationEmail: Activation, val orderRules: OrderRules) {

    constructor(orderConfirmationEmail: Activation) : this(orderConfirmationEmail, OrderRules.byDefault())

    val paymentDelay get() = orderRules.paymentDelay

    val freeShippingThreshold get() = orderRules.freeShippingThreshold

    fun sendsOrderConfirmationEmail() = orderConfirmationEmail == Activation.ENABLED

    fun withOrderConfirmationEmailEnabled() = copy(orderConfirmationEmail = Activation.ENABLED)

    fun withOrderConfirmationEmailDisabled() = copy(orderConfirmationEmail = Activation.DISABLED)

    fun withPaymentDelay(paymentDelay: PaymentDelay) =
        copy(orderRules = orderRules.copy(paymentDelay = paymentDelay))

    fun withFreeShippingThreshold(freeShippingThreshold: FreeShippingThreshold) =
        copy(orderRules = orderRules.copy(freeShippingThreshold = freeShippingThreshold))

    companion object {
        fun byDefault() = ShopSettings(orderConfirmationEmail = Activation.ENABLED)
    }
}

// An administrator who changes the settings of the shop
data class ShopSettingsUpdate(val editor: UserId, val settings: ShopSettings)

// An administrator who reads the settings of the shop, to change them
data class ShopSettingsReading(val reader: UserId)

data class ShopSettingsAdministrationForbiddenException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class InvalidFreeShippingThresholdException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class InvalidPaymentDelayException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ShopSettingsUpdateFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
