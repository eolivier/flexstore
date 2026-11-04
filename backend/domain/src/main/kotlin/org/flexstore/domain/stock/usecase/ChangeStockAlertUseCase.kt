package org.flexstore.domain.stock.usecase

import org.flexstore.domain.product.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator gives the number of units at or under which they are warned that a product runs low
class ChangeStockAlertUseCase(
    private val stockRepository: StockRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : UseCase<StockAlertChange> {

    override fun getPreConditions(): PreConditions<StockAlertChange> = PreConditions(
        editorIsAdministratorCondition(), productExistsCondition(), thresholdIsNotNegativeCondition()
    )

    override fun getNominalScenario(): NominalScenario<StockAlertChange> =
        NominalScenario(Step { change -> stockRepository.saveAlert(change.stockAlert) })

    override fun getPostConditions(): PostConditions<StockAlertChange> =
        PostConditions(stockAlertIsStoredCondition())

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<StockAlertChange> { change ->
        if (userRepository.isNotAdministrator(change.editor)) {
            throw StockAlertForbiddenException(
                ErrorMessage("Only an administrator can change the alert threshold of a product.")
            )
        }
    }

    private fun productExistsCondition() = PreCondition<StockAlertChange> { change ->
        val productId = change.stockAlert.productId
        if (productRepository.productIsMissing(productId)) {
            throw ProductNotFoundException(ErrorMessage("Product with ID ${productId.id.value} does not exist."))
        }
    }

    private fun thresholdIsNotNegativeCondition() = PreCondition<StockAlertChange> { change ->
        val threshold = change.stockAlert.threshold
        if (threshold.isNegative()) {
            val message = ErrorMessage("An alert threshold cannot be negative: ${threshold.value}.")
            throw NegativeAlertThresholdException(message)
        }
    }

    private fun stockAlertIsStoredCondition() = PostCondition<StockAlertChange> { change ->
        val stockAlert = change.stockAlert
        if (stockRepository.alertOf(stockAlert.productId) != stockAlert) {
            val message = ErrorMessage("The alert threshold of ${stockAlert.productId.id.value} was not changed.")
            throw StockAlertChangeFailed(message)
        }
    }
}
