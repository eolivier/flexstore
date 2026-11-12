package org.flexstore.domain.stock.usecase

import org.flexstore.domain.stock.*
import org.flexstore.domain.user.model.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator reads the threshold of the stock of a product: the default one when it was never given
class ReadStockAlertUseCase(
    private val stockRepository: StockRepository
) : QueryUseCase<StockAlertReading, StockAlert> {

    override fun getPreConditions(): PreConditions<StockAlertReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<StockAlertReading, StockAlert> { reading ->
        stockRepository.alertOf(reading.productId)
    }

    override fun getPostConditions(): PostConditions<StockAlert> = PostConditions.none()

    // The reader, resolved before the use case runs, must be an Admin
    private fun readerIsAdministratorCondition() = PreCondition<StockAlertReading> { reading ->
        if (reading.reader.isNotAdmin()) {
            throw StockAlertForbiddenException(
                ErrorMessage("Only an administrator can read the alert threshold of a product.")
            )
        }
    }
}
