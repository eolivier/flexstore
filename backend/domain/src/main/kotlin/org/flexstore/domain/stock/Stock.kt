package org.flexstore.domain.stock

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.sharedkernel.Quantity
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.entity.Actor

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

// The number of units at or under which the administrators are warned that a product runs low: never negative
data class AlertThreshold(val value: Int) {

    fun isNegative() = value < 0

    fun isReachedBy(quantity: StockQuantity) = quantity.value <= value

    companion object {
        private const val DEFAULT_UNITS = 5

        // The threshold of a product whose threshold was never given
        fun byDefault() = AlertThreshold(DEFAULT_UNITS)
    }
}

// The threshold of the stock of a product, given by an administrator
data class StockAlert(val productId: ProductId, val threshold: AlertThreshold) {

    companion object {
        fun byDefaultOf(productId: ProductId) = StockAlert(productId, AlertThreshold.byDefault())
    }
}

// An administrator who gives the threshold of the stock of a product
data class StockAlertChange(val editor: Actor, val stockAlert: StockAlert)

// An administrator who reads the threshold of the stock of a product
data class StockAlertReading(val reader: Actor, val productId: ProductId)

// An administrator who reads the stocks that reached the threshold of their product
data class LowStocksReading(val reader: UserId)

// A stock that reached the threshold of its product
data class LowStock(val stock: Stock, val threshold: AlertThreshold)

// The stocks that reached their threshold, from the lowest, for the administrators
data class LowStocks(private val lowStocks: List<LowStock>) {

    constructor(vararg lowStocks: LowStock) : this(lowStocks.toList())

    // Read-only view for the adapters (JSON)
    fun asList(): List<LowStock> = lowStocks.sortedBy { it.stock.quantity.value }
}

// The units of a reservation are taken out of the stock only if they are all there, at the moment they are
// taken: another order may have taken them since the stock was read
enum class StockReservation {
    RESERVED, NOT_COVERED;

    fun isNotCovered() = this == NOT_COVERED
}

// An administrator who gives the number of units of a product that the shop has
data class StockChange(val editor: UserId, val stock: Stock)

data class StockChangeForbiddenException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class OutOfStockException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class NegativeStockException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class NegativeAlertThresholdException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class StockAlertForbiddenException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class StockAlertChangeFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class StockChangeFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
