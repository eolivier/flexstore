package org.flexstore.infra.repository

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Quantity
import org.flexstore.domain.stock.*

class InMemoryStockRepository : StockRepository {

    private val stocks = mutableMapOf<ProductId, Stock>()

    override fun stockOf(productId: ProductId) = stocks[productId] ?: Stock.noneOf(productId)

    override fun findAll() = Stocks(stocks.values.toList())

    override fun save(stock: Stock) {
        stocks[stock.productId] = stock
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
