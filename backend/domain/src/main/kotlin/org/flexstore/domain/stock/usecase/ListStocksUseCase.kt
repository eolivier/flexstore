package org.flexstore.domain.stock.usecase

import org.flexstore.domain.stock.*
import org.ucop.domain.entity.*

// The stocks of all the products that have one
class ListStocksUseCase(private val stockRepository: StockRepository) : QueryUseCase<Unit, Stocks> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, Stocks> { stockRepository.findAll() }

    override fun getPostConditions(): PostConditions<Stocks> = PostConditions.none()
}
