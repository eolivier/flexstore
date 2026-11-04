package org.flexstore.domain.stock.usecase

import org.flexstore.domain.product.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator gives the number of units of a product that the shop has
class ChangeStockUseCase(
    private val stockRepository: StockRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : UseCase<StockChange> {

    override fun getPreConditions(): PreConditions<StockChange> =
        PreConditions(
            editorIsAdministratorCondition(), productExistsCondition(), quantityIsNotNegativeCondition()
        )

    override fun getNominalScenario(): NominalScenario<StockChange> =
        NominalScenario(Step { (_, stock) -> stockRepository.save(stock) })

    override fun getPostConditions(): PostConditions<StockChange> = PostConditions(stockIsStoredCondition())

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<StockChange> { request ->
        if (userRepository.isNotAdministrator(request.editor)) {
            throw StockChangeForbiddenException(ErrorMessage("Only an administrator can change the stock of a product."))
        }
    }

    private fun productExistsCondition() = PreCondition<StockChange> { (_, stock) ->
        if (productRepository.productIsMissing(stock.productId)) {
            val message = ErrorMessage("Product with ID ${stock.productId.id.value} does not exist.")
            throw ProductNotFoundException(message)
        }
    }

    private fun quantityIsNotNegativeCondition() = PreCondition<StockChange> { (_, stock) ->
        if (stock.quantity.isNegative()) {
            throw NegativeStockException(ErrorMessage("A stock cannot be negative: ${stock.quantity.value}."))
        }
    }

    private fun stockIsStoredCondition() = PostCondition<StockChange> { (_, stock) ->
        if (stockRepository.stockOf(stock.productId) != stock) {
            throw StockChangeFailed(ErrorMessage("The stock of ${stock.productId.id.value} was not changed."))
        }
    }
}
