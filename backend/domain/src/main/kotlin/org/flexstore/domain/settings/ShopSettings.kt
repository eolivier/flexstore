package org.flexstore.domain.settings

import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal

import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException
import org.ucop.domain.ErrorMessage
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

// How long after its last change a cart is abandoned, in hours: its customer is then reminded of it.
// From 1 hour to 30 days
data class AbandonedCartDelay(val hours: Int) {

    fun isValid() = hours in 1..MAX_HOURS

    fun isInvalid() = !isValid()

    fun asDuration(): Duration = Duration.ofHours(hours.toLong())

    companion object {
        const val MAX_HOURS = 30 * 24
        private const val DEFAULT_HOURS = 24

        fun byDefault() = AbandonedCartDelay(DEFAULT_HOURS)
    }
}

// The reminder sent by email to the customer who left items in their cart: turned on or off, then sent once the
// cart has been left for the delay
data class AbandonedCartReminder(val activation: Activation, val delay: AbandonedCartDelay) {
    companion object {
        // Off by default: the shop decides to write to its customers
        fun byDefault() = AbandonedCartReminder(Activation.DISABLED, AbandonedCartDelay.byDefault())
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

// The settings of the shop, chosen by the administrators, except the seller setup: it is turned on by the setup
// assistant of the seller once finished, and never changed by the administrators
data class ShopSettings(
    val orderConfirmationEmail: Activation,
    val orderRules: OrderRules,
    val sellerSetup: Activation,
    val abandonedCartReminder: AbandonedCartReminder
) {

    constructor(orderConfirmationEmail: Activation, orderRules: OrderRules, sellerSetup: Activation) :
        this(orderConfirmationEmail, orderRules, sellerSetup, AbandonedCartReminder.byDefault())

    constructor(orderConfirmationEmail: Activation) :
        this(orderConfirmationEmail, OrderRules.byDefault(), Activation.DISABLED)

    constructor(orderConfirmationEmail: Activation, orderRules: OrderRules) :
        this(orderConfirmationEmail, orderRules, Activation.DISABLED)

    val paymentDelay get() = orderRules.paymentDelay

    val freeShippingThreshold get() = orderRules.freeShippingThreshold

    val abandonedCartDelay get() = abandonedCartReminder.delay

    fun sendsAbandonedCartReminder() = abandonedCartReminder.activation == Activation.ENABLED

    fun sendsOrderConfirmationEmail() = orderConfirmationEmail == Activation.ENABLED

    fun isSellerSetUp() = sellerSetup == Activation.ENABLED

    fun isSellerNotSetUp() = !isSellerSetUp()

    fun withSellerSetUp() = copy(sellerSetup = Activation.ENABLED)

    fun withOrderConfirmationEmailEnabled() = copy(orderConfirmationEmail = Activation.ENABLED)

    fun withOrderConfirmationEmailDisabled() = copy(orderConfirmationEmail = Activation.DISABLED)

    fun withAbandonedCartReminderEnabled() =
        copy(abandonedCartReminder = abandonedCartReminder.copy(activation = Activation.ENABLED))

    fun withAbandonedCartReminderDisabled() =
        copy(abandonedCartReminder = abandonedCartReminder.copy(activation = Activation.DISABLED))

    fun withAbandonedCartDelay(abandonedCartDelay: AbandonedCartDelay) =
        copy(abandonedCartReminder = abandonedCartReminder.copy(delay = abandonedCartDelay))

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
    PreConditionException(errorMessage)
data class InvalidFreeShippingThresholdException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class InvalidAbandonedCartDelayException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class InvalidPaymentDelayException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class ShopSettingsUpdateFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)
