package org.flexstore.domain.stock

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.sharedkernel.Quantity
import org.ucop.domain.NominalException

// The number of units of a product that the shop has: never negative
data class StockQuantity(val value: Int) {

    fun covers(quantity: Quantity) = value >= quantity.value

    fun isNegative() = value < 0

    fun isEmpty() = value == 0

    // The units left once the given quantity is taken: none rather than a negative number
    fun minus(quantity: Quantity) = StockQuantity(maxOf(0, value - quantity.value))

    companion object {
        fun none() = StockQuantity(0)
    }
}

// The stock of a product of the catalog
data class Stock(val productId: ProductId, val quantity: StockQuantity) {

    fun covers(quantity: Quantity) = this.quantity.covers(quantity)

    fun doesNotCover(quantity: Quantity) = !covers(quantity)

    // A paid order takes its units out of the stock, which never goes below zero
    // (an order paid while another one took the last units)
    fun withdrawn(quantity: Quantity) = copy(quantity = this.quantity.minus(quantity))

    companion object {
        // A product whose stock was never given has none
        fun noneOf(productId: ProductId) = Stock(productId, StockQuantity.none())
    }
}

// The stocks of the products of the catalog
data class Stocks(private val stocks: List<Stock>) {

    constructor(vararg stocks: Stock) : this(stocks.toList())

    fun of(productId: ProductId) = stocks.firstOrNull { it.productId == productId } ?: Stock.noneOf(productId)

    // Read-only view for the adapters (JSON)
    fun asList(): List<Stock> = stocks.toList()
}

data class OutOfStockException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class NegativeStockException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class StockChangeFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
