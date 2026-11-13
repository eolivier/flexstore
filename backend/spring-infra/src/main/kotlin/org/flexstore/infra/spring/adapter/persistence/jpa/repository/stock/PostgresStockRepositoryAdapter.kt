package org.flexstore.infra.spring.adapter.persistence.jpa.repository.stock

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.Quantity
import org.flexstore.domain.stock.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class PostgresStockRepositoryAdapter(private val jpaRepo: JpaStockRepository) : StockRepository {

    @Transactional(readOnly = true)
    override fun stockOf(productId: ProductId): Stock =
        jpaRepo.findStockOf(productId.toString())?.toStock() ?: Stock.noneOf(productId)

    @Transactional(readOnly = true)
    override fun findAll() = Stocks(jpaRepo.findGivenStocks().map { it.toStock() })

    override fun save(stock: Stock) {
        jpaRepo.giveQuantity(stock.productId.toString(), stock.quantity.value)
    }

    @Transactional(readOnly = true)
    override fun alertOf(productId: ProductId): StockAlert =
        jpaRepo.findStockOf(productId.toString())?.toAlert() ?: StockAlert.byDefaultOf(productId)

    override fun saveAlert(stockAlert: StockAlert) {
        jpaRepo.giveThreshold(stockAlert.productId.toString(), stockAlert.threshold.value)
    }

    @Transactional(readOnly = true)
    override fun findLow() =
        LowStocks(jpaRepo.findLowStocks().map { LowStock(it.toStock(), it.toAlert().threshold) })

    override fun reserve(productId: ProductId, quantity: Quantity): StockReservation {
        val updatedRows = jpaRepo.takeOut(productId.toString(), quantity.value)
        return if (updatedRows == 1) StockReservation.RESERVED else StockReservation.NOT_COVERED
    }

    override fun release(productId: ProductId, quantity: Quantity) {
        jpaRepo.putBack(productId.toString(), quantity.value)
    }

    private fun StockRow.toStock() =
        Stock(ProductId(Identity(productId)), quantity?.let { StockQuantity(it) } ?: StockQuantity.none())

    private fun StockRow.toAlert() = StockAlert(ProductId(Identity(productId)), AlertThreshold(threshold))
}
