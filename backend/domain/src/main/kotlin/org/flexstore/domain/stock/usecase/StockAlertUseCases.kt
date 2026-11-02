package org.flexstore.domain.stock.usecase

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.model.UserId

// The use cases of the alerts on the stocks, for the administrators: the threshold of a product is read back
// once changed
class StockAlertUseCases(
    private val listLowStocksUseCase: ListLowStocksUseCase,
    private val readStockAlertUseCase: ReadStockAlertUseCase,
    private val changeStockAlertUseCase: ChangeStockAlertUseCase
) {
    fun lowStocks(reader: UserId) = listLowStocksUseCase.unfold(LowStocksReading(reader))

    fun alertOf(reader: UserId, productId: ProductId) =
        readStockAlertUseCase.unfold(StockAlertReading(reader, productId))

    fun change(editor: UserId, stockAlert: StockAlert): StockAlert {
        changeStockAlertUseCase.unfold(StockAlertChange(editor, stockAlert))
        return readStockAlertUseCase.unfold(StockAlertReading(editor, stockAlert.productId))
    }
}
