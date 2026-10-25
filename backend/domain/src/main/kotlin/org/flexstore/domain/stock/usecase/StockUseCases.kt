package org.flexstore.domain.stock.usecase

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.stock.*

// The use cases of the stocks: the stock of a product is read back once changed
class StockUseCases(
    private val readStockUseCase: ReadStockUseCase,
    private val changeStockUseCase: ChangeStockUseCase
) {
    fun getStocks() = readStockUseCase.getStocks()

    fun getStock(productId: ProductId) = readStockUseCase.getStock(productId)

    fun change(stock: Stock): Stock {
        changeStockUseCase.unfold(stock)
        return readStockUseCase.getStock(stock.productId)
    }
}
