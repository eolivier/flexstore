package org.flexstore.domain.stock.usecase

import org.flexstore.domain.actor.verifyCanRead
import org.flexstore.domain.stock.*
import org.ucop.domain.entity.*

// The stocks of all the products that have one
class ListStocksUseCase(private val stockRepository: StockRepository) : QueryUseCase<Actor, Stocks> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(readerCanReadCondition())

    override fun getNominalQuery() = NominalQuery<Actor, Stocks> { stockRepository.findAll() }

    override fun getPostConditions(): PostConditions<Stocks> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<Actor> { reader ->
        reader.verifyCanRead("the stocks", ::StockReadingForbiddenException)
    }
}
