package org.flexstore.domain.sharedkernel

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class Price(val amount: Amount, val currency: Currency) {
    fun multiply(quantity: Quantity) = Price(amount.multiply(quantity), currency)

    // The sum of two prices in the same currency
    operator fun plus(other: Price) = Price(Amount(amount.value.add(other.amount.value)), currency)

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
}
data class Quantity(val value: Int) {
    fun addNewQuantity(newQuantity: Quantity) = Quantity(this.value + newQuantity.value)
    fun increase() = Quantity(value + 1)
    fun decrease() = Quantity(value - 1)
    fun changeQuantity(newQuantity: Quantity) = Quantity(newQuantity.value)
    fun isNegative() = value < 0
    fun isZero() = value == 0
    fun isPositive() = value > 0
    fun isZeroOrNegative() = value <= 0
    fun isAtLeast(other: Quantity) = value >= other.value
}
data class Identity(val value: String) {
    companion object {
        // A new unique identity: every new id of the application comes from here
        fun nextId() = Identity(java.util.UUID.randomUUID().toString())
    }
}
enum class Currency(val symbol: String) {
    EUR("€"), USD("$");
    companion object {
        fun fromSymbol(symbol: String): Currency =
            entries.find { it.symbol == symbol }
                ?: throw IllegalArgumentException("Unknown currency for symbol : $symbol")
    }
}

data class Name(val value: String) {
    init {
        require(value.isNotBlank()) { "Name must not ne empty nor null" }
    }
}

data class Description(val value: String) {
    init {
        require(value.isNotBlank()) { "Description must not ne empty nor null" }
    }
}

// The message of a domain exception: never empty, so that every failure says what happened
data class ErrorMessage(val value: String) {
    init {
        require(value.isNotEmpty()) { "An error message must not be empty" }
    }
}
