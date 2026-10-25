package org.flexstore.infra.repository

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.stock.*

class InMemoryStockRepository : StockRepository {

    private val stocks = mutableMapOf<ProductId, Stock>()

    override fun stockOf(productId: ProductId) = stocks[productId] ?: Stock.noneOf(productId)

    override fun findAll() = Stocks(stocks.values.toList())

    override fun save(stock: Stock) {
        stocks[stock.productId] = stock
    }
}
