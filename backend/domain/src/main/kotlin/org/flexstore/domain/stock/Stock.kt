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

    // The part of the quantity that the units cover: all of it, or the units left
    fun availableOf(quantity: Quantity) = Quantity(minOf(value, quantity.value))

    companion object {
        fun none() = StockQuantity(0)
    }
}

// The stock of a product of the catalog
data class Stock(val productId: ProductId, val quantity: StockQuantity) {

    fun covers(quantity: Quantity) = this.quantity.covers(quantity)

    fun doesNotCover(quantity: Quantity) = !covers(quantity)

    // An order paid after it was cancelled takes back what is left of its units, as another order may have
    // taken the last ones in the meantime
    fun availableOf(quantity: Quantity) = this.quantity.availableOf(quantity)

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

// The units of a reservation are taken out of the stock only if they are all there, at the moment they are
// taken: another order may have taken them since the stock was read
enum class StockReservation {
    RESERVED, NOT_COVERED;

    fun isNotCovered() = this == NOT_COVERED
}

data class OutOfStockException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class NegativeStockException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class StockChangeFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
