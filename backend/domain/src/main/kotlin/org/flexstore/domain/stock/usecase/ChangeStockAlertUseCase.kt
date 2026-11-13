package org.flexstore.domain.stock.usecase

import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator gives the number of units at or under which they are warned that a product runs low
class ChangeStockAlertUseCase(
    private val stockRepository: StockRepository,
    private val productRepository: ProductRepository
) : UseCase<StockAlertChange> {

    override fun getPreConditions(): PreConditions<StockAlertChange> = PreConditions(
        editorIsAdministratorCondition(), productExistsCondition(), thresholdIsNotNegativeCondition()
    )

    override fun getNominalScenario(): NominalScenario<StockAlertChange> =
        NominalScenario(Step { change -> stockRepository.saveAlert(change.stockAlert) })

    override fun getPostConditions(): PostConditions<StockAlertChange> =
        PostConditions(stockAlertIsStoredCondition())

    // The editor, resolved before the use case runs, must be an Admin
    private fun editorIsAdministratorCondition() = PreCondition<StockAlertChange> { change ->
        if (change.editor.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can change the alert threshold of a product.")
            throw StockAlertForbiddenException(message)
        }
    }

    private fun productExistsCondition() = PreCondition<StockAlertChange> { change ->
        val productId = change.stockAlert.productId
        if (productRepository.productIsMissing(productId)) {
            throw ProductNotFoundException(ErrorMessage("Product with ID ${productId} does not exist."))
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
            val reason = "The alert threshold of ${stockAlert.productId} was not changed."
            val message = ErrorMessage(reason)
            throw StockAlertChangeFailed(message)
        }
    }
}
