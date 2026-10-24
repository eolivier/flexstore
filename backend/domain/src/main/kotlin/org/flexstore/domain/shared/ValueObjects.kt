package org.flexstore.domain.shared

import java.math.BigDecimal

data class Price(val amount: Amount, val currency: Currency) {
    fun multiply(quantity: Quantity) = Price(amount.multiply(quantity), currency)
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
