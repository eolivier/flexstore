package org.flexstore.domain.stock.usecase

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.service.ActorResolutionService

// The use cases of the alerts on the stocks, for the administrators: the threshold of a product is read back
// once changed
class StockAlertUseCases(
    private val listLowStocksUseCase: ListLowStocksUseCase,
    private val readStockAlertUseCase: ReadStockAlertUseCase,
    private val changeStockAlertUseCase: ChangeStockAlertUseCase,
    private val actorResolutionService: ActorResolutionService
) {
    fun lowStocks(reader: UserId) = listLowStocksUseCase.unfold(LowStocksReading(reader))

    fun alertOf(reader: UserId, productId: ProductId) =
        readStockAlertUseCase.unfold(StockAlertReading(actorResolutionService.actorOf(reader), productId))

    // The editor is resolved once, then reads back what they changed
    fun change(editor: UserId, stockAlert: StockAlert): StockAlert {
        val actor = actorResolutionService.actorOf(editor)
        changeStockAlertUseCase.unfold(StockAlertChange(actor, stockAlert))
        return readStockAlertUseCase.unfold(StockAlertReading(actor, stockAlert.productId))
    }
}
