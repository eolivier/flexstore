package org.flexstore.domain.discount

import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.NominalException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

// The code a customer enters to get a discount (BIENVENUE10): upper-case letters and digits, 3 to 30
data class PromoCode(val value: String) {

    fun isWellFormed() = WELL_FORMED.matches(value)

    fun isMalformed() = !isWellFormed()

    companion object {
        private val WELL_FORMED = Regex("[A-Z0-9]{3,30}")

        // As typed: the spaces around are dropped, the letters put in upper case
        fun from(typedCode: String) = PromoCode(typedCode.trim().uppercase())
    }
}

// A percentage, from more than 0 to 100
data class Percentage(val value: BigDecimal) {
    fun isValid() = value.signum() > 0 && value <= BigDecimal(100)
}

// What a discount code takes off the price of the items of an order, never more than that price
sealed interface Reduction {
    fun amountOff(itemsPrice: Price): Price
    fun isValid(): Boolean
}

// A percentage of the price of the items, rounded to the cent
data class PercentageOff(val percentage: Percentage) : Reduction {

    override fun amountOff(itemsPrice: Price): Price {
        val share = itemsPrice.amount.value.multiply(percentage.value)
        val amount = share.divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        return Price(Amount(amount), itemsPrice.currency)
    }

    override fun isValid() = percentage.isValid()
}

// A fixed amount, up to the price of the items
data class AmountOff(val amount: Price) : Reduction {

    override fun amountOff(itemsPrice: Price): Price = if (itemsPrice.isAtLeast(amount)) amount else itemsPrice

    override fun isValid() = amount.amount.value.signum() > 0
}

// The moment a discount code stops being usable
data class ExpiryDate(val value: Instant) {
    fun isReachedAt(instant: Instant) = instant >= value
}

// How many orders may use a discount code, at least 1
data class UsageLimit(val value: Int) {
    fun isValid() = value >= 1
}

// How many orders used a discount code
data class UseCount(val value: Int) {
    fun next() = UseCount(value + 1)

    fun hasReached(usageLimit: UsageLimit) = value >= usageLimit.value

    companion object {
        fun none() = UseCount(0)
    }
}

// Until when, and for how many orders, a discount code is usable
data class DiscountValidity(val expiresAt: ExpiryDate, val usageLimit: UsageLimit)

// What a discount code gives, and its validity
data class DiscountOffer(val reduction: Reduction, val validity: DiscountValidity)

// A discount code given by the administrators: what it takes off the items of an order, until when and for how
// many orders, and how many orders used it already
data class DiscountCode(val code: PromoCode, val offer: DiscountOffer, val uses: UseCount) {

    // A new code, used by no order yet
    constructor(code: PromoCode, offer: DiscountOffer) : this(code, offer, UseCount.none())

    val reduction get() = offer.reduction

    fun isExpiredAt(instant: Instant) = offer.validity.expiresAt.isReachedAt(instant)

    fun isUsedUp() = uses.hasReached(offer.validity.usageLimit)

    fun usedOnceMore() = copy(uses = uses.next())
}

// A discount code looked for at a given moment, to apply it to an order
data class DiscountCodeLookup(val code: PromoCode, val at: Instant)

data class InvalidDiscountCodeException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class DiscountCodeAlreadyExistsException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class DiscountCodeNotFoundException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class DiscountCodeExpiredException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class DiscountCodeUsedUpException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class DiscountCodeCreationFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
