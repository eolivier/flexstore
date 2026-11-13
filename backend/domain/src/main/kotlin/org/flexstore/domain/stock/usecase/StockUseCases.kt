package org.flexstore.domain.stock.usecase

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.stock.*
import org.ucop.domain.entity.Actor

// The use cases of the stocks: the stock of a product is read back once changed
class StockUseCases(
    private val listStocksUseCase: ListStocksUseCase,
    private val readStockUseCase: ReadStockUseCase,
    private val changeStockUseCase: ChangeStockUseCase
) {
    fun getStocks(reader: Actor) = listStocksUseCase.unfold(reader)

    fun getStock(reader: Actor, productId: ProductId) =
        readStockUseCase.unfold(StockReading(reader, productId))

    fun change(editor: Actor, stock: Stock): Stock {
        changeStockUseCase.unfold(StockChange(editor, stock))
        return readStockUseCase.unfold(StockReading(editor, stock.productId))
    }
}
