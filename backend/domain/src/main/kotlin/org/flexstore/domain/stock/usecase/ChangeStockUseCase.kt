package org.flexstore.domain.stock.usecase

import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.stock.*
import org.ucop.domain.entity.*

// An administrator gives the number of units of a product that the shop has
class ChangeStockUseCase(
    private val stockRepository: StockRepository,
    private val productRepository: ProductRepository
) : UseCase<Stock> {

    override fun getPreConditions(): PreConditions<Stock> =
        PreConditions(productExistsCondition(), quantityIsNotNegativeCondition())

    override fun getNominalScenario(): NominalScenario<Stock> =
        NominalScenario(Step { stock -> stockRepository.save(stock) })

    override fun getPostConditions(): PostConditions<Stock> = PostConditions(stockIsStoredCondition())

    private fun productExistsCondition() = PreCondition<Stock> { stock ->
        if (productRepository.productIsMissing(stock.productId)) {
            val message = ErrorMessage("Product with ID ${stock.productId.id.value} does not exist.")
            throw ProductNotFoundException(message)
        }
    }

    private fun quantityIsNotNegativeCondition() = PreCondition<Stock> { stock ->
        if (stock.quantity.isNegative()) {
            throw NegativeStockException(ErrorMessage("A stock cannot be negative: ${stock.quantity.value}."))
        }
    }

    private fun stockIsStoredCondition() = PostCondition<Stock> { stock ->
        if (stockRepository.stockOf(stock.productId) != stock) {
            throw StockChangeFailed(ErrorMessage("The stock of ${stock.productId.id.value} was not changed."))
        }
    }
}
