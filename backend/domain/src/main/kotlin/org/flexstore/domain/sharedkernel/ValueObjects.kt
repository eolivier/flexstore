package org.flexstore.domain.sharedkernel

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

data class Price(val amount: Amount, val currency: Currency) {
    fun multiply(quantity: Quantity) = Price(amount.multiply(quantity), currency)

    // The sum of two prices in the same currency
    operator fun plus(other: Price) = Price(Amount(amount.value.add(other.amount.value)), currency)

    // The difference of two prices in the same currency
    operator fun minus(other: Price) = Price(Amount(amount.value.subtract(other.amount.value)), currency)

    fun isFree() = amount.value.signum() == 0

    // The whole euros or dollars of the price, without its cents
    fun wholeUnits() = Quantity(amount.value.setScale(0, RoundingMode.DOWN).toInt())

    fun isAtLeast(other: Price) = amount.value >= other.amount.value

    // The price as the customer reads it in the given language, with 2 decimals (e.g. 1 598,00 € in French)
    fun formattedIn(locale: Locale): String {
        val format = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "${format.format(amount.value)} ${currency.symbol}"
    }
}
data class Amount(val value: BigDecimal) {
    fun multiply(quantity: Quantity) = Amount(this.value.multiply(BigDecimal(quantity.value)))

    companion object {
        // An amount of money is given to the cent: 2 digits after the decimal point
        const val CENT_SCALE = 2
    }
}
data class Quantity(val value: Int) {
    fun increase() = Quantity(value + 1)
    fun decrease() = Quantity(value - 1)
    fun changeQuantity(newQuantity: Quantity) = Quantity(newQuantity.value)
    fun isNegative() = value < 0
    fun isZero() = value == 0
    fun isZeroOrNegative() = value <= 0
    fun isAtLeast(other: Quantity) = value >= other.value
}
data class Identity(val value: String) {
    companion object {
        // A new unique identity: every new id of the application comes from here
        fun nextId() = Identity(java.util.UUID.randomUUID().toString())
    }
}
// An id value object: it wraps an Identity, and its text (toString) is the one of its identity, which is what the
// database and the JSON store
abstract class Identifiable {
    abstract val identity: Identity

    final override fun toString() = identity.value
}

enum class Currency(val symbol: String) {
    EUR("€"), USD("$")
}

data class Name(val value: String) {
    init {
        require(value.isNotBlank()) { "Name must not be empty nor null" }
    }
}

data class Description(val value: String) {
    init {
        require(value.isNotBlank()) { "Description must not be empty nor null" }
    }
}
