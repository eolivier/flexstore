package org.flexstore.domain.stock

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Quantity

interface StockRepository {

    // The stock of a product: none when it was never given
    fun stockOf(productId: ProductId): Stock

    fun findAll(): Stocks

    // The stock given by an administrator
    fun save(stock: Stock)

    // Takes the units out of the stock in one operation, so that two orders cannot take the same units
    fun reserve(productId: ProductId, quantity: Quantity): StockReservation

    // Gives the units back to the stock in one operation, without overwriting a reservation made meanwhile
    fun release(productId: ProductId, quantity: Quantity)
}
