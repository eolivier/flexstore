package org.flexstore.domain.stock.usecase

import org.flexstore.domain.stock.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator reads the threshold of the stock of a product: the default one when it was never given
class ReadStockAlertUseCase(
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository
) : QueryUseCase<StockAlertReading, StockAlert> {

    override fun getPreConditions(): PreConditions<StockAlertReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<StockAlertReading, StockAlert> { reading ->
        stockRepository.alertOf(reading.productId)
    }

    override fun getPostConditions(): PostConditions<StockAlert> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<StockAlertReading> { reading ->
        if (userRepository.isNotAdministrator(reading.reader)) {
            throw StockAlertForbiddenException(
                ErrorMessage("Only an administrator can read the alert threshold of a product.")
            )
        }
    }
}
