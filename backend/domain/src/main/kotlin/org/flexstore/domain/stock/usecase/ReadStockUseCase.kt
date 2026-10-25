package org.flexstore.domain.stock.usecase

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.stock.*
import org.ucop.domain.UseCaseLog

class ReadStockUseCase(private val stockRepository: StockRepository) {

    fun getStocks(): Stocks = UseCaseLog.execution("ReadStockUseCase.getStocks") { stockRepository.findAll() }

    fun getStock(productId: ProductId): Stock =
        UseCaseLog.execution("ReadStockUseCase.getStock") { stockRepository.stockOf(productId) }
}
