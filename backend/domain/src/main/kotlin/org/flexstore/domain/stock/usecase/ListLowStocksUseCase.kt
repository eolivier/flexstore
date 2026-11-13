package org.flexstore.domain.stock.usecase

import org.flexstore.domain.stock.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator reads the stocks that reached the threshold of their product, which they are warned of
class ListLowStocksUseCase(
    private val stockRepository: StockRepository
) : QueryUseCase<LowStocksReading, LowStocks> {

    override fun getPreConditions(): PreConditions<LowStocksReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<LowStocksReading, LowStocks> { stockRepository.findLow() }

    override fun getPostConditions(): PostConditions<LowStocks> = PostConditions.none()

    // The reader, resolved before the use case runs, must be an Admin
    private fun readerIsAdministratorCondition() = PreCondition<LowStocksReading> { reading ->
        if (reading.reader.isNotAdmin()) {
            throw StockAlertForbiddenException(ErrorMessage("Only an administrator can read the low stocks."))
        }
    }
}
