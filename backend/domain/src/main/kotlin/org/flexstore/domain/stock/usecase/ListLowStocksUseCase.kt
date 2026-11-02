package org.flexstore.domain.stock.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.entity.*

// An administrator reads the stocks that reached the threshold of their product, which they are warned of
class ListLowStocksUseCase(
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository
) : QueryUseCase<LowStocksReading, LowStocks> {

    override fun getPreConditions(): PreConditions<LowStocksReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<LowStocksReading, LowStocks> { stockRepository.findLow() }

    override fun getPostConditions(): PostConditions<LowStocks> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<LowStocksReading> { reading ->
        if (userRepository.isNotAdministrator(reading.reader)) {
            throw StockAlertForbiddenException(ErrorMessage("Only an administrator can read the low stocks."))
        }
    }
}
