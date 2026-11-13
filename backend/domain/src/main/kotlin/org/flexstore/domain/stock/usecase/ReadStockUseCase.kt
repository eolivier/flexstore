package org.flexstore.domain.stock.usecase

import org.flexstore.domain.actor.cannotReadPublicContent
import org.ucop.domain.ErrorMessage
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.stock.*
import org.ucop.domain.entity.*

// The stock of a product: none when it was never given
// A reader asks for the stock of a product
data class StockReading(val reader: Actor, val productId: ProductId)

class ReadStockUseCase(private val stockRepository: StockRepository) : QueryUseCase<StockReading, Stock> {

    override fun getPreConditions(): PreConditions<StockReading> = PreConditions(readerCanReadCondition())

    override fun getNominalQuery() = NominalQuery<StockReading, Stock> { reading ->
        stockRepository.stockOf(reading.productId)
    }

    override fun getPostConditions(): PostConditions<Stock> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<StockReading> { reading ->
        if (reading.reader.cannotReadPublicContent()) {
            val message = ErrorMessage("Only a visitor, a user or the system can read the stocks.")
            throw StockReadingForbiddenException(message)
        }
    }
}
