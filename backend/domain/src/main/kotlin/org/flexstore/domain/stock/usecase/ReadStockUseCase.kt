package org.flexstore.domain.stock.usecase

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.stock.*
import org.ucop.domain.entity.*

// The stock of a product: none when it was never given
class ReadStockUseCase(private val stockRepository: StockRepository) : QueryUseCase<ProductId, Stock> {

    override fun getPreConditions(): PreConditions<ProductId> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<ProductId, Stock> { productId ->
        stockRepository.stockOf(productId)
    }

    override fun getPostConditions(): PostConditions<Stock> = PostConditions.none()
}
