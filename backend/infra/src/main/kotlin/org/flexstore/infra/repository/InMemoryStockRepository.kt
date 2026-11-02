package org.flexstore.infra.repository

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Quantity
import org.flexstore.domain.stock.*

class InMemoryStockRepository : StockRepository {

    private val stocks = mutableMapOf<ProductId, Stock>()

    private val alerts = mutableMapOf<ProductId, StockAlert>()

    override fun stockOf(productId: ProductId) = stocks[productId] ?: Stock.noneOf(productId)

    override fun findAll() = Stocks(stocks.values.toList())

    override fun save(stock: Stock) {
        stocks[stock.productId] = stock
    }

    override fun alertOf(productId: ProductId) = alerts[productId] ?: StockAlert.byDefaultOf(productId)

    override fun saveAlert(stockAlert: StockAlert) {
        alerts[stockAlert.productId] = stockAlert
    }

    // As in the database, only the products whose stock was given
    override fun findLow(): LowStocks {
        val lowStocks = stocks.values
            .map { stock -> LowStock(stock, alertOf(stock.productId).threshold) }
            .filter { lowStock -> lowStock.threshold.isReachedBy(lowStock.stock.quantity) }
        return LowStocks(lowStocks)
    }

    // Synchronized: the check and the update happen together, as in the database
    @Synchronized
    override fun reserve(productId: ProductId, quantity: Quantity): StockReservation {
        val stock = stockOf(productId)
        if (stock.doesNotCover(quantity)) return StockReservation.NOT_COVERED
        stocks[productId] = Stock(productId, StockQuantity(stock.quantity.value - quantity.value))
        return StockReservation.RESERVED
    }

    @Synchronized
    override fun release(productId: ProductId, quantity: Quantity) {
        val stock = stockOf(productId)
        stocks[productId] = Stock(productId, StockQuantity(stock.quantity.value + quantity.value))
    }
}
