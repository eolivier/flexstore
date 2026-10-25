package org.flexstore.domain.stock

import org.flexstore.domain.product.ProductId

interface StockRepository {

    // The stock of a product: none when it was never given
    fun stockOf(productId: ProductId): Stock

    fun findAll(): Stocks

    fun save(stock: Stock)
}
