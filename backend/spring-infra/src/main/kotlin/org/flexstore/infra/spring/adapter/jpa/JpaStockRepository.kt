package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.stock.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaStockRepository : JpaRepository<StockEntity, String> {
    fun findByProductId(productId: String): StockEntity?
}

@Repository
@Transactional
class PostgresStockRepositoryAdapter(private val jpaRepo: JpaStockRepository) : StockRepository {

    @Transactional(readOnly = true)
    override fun stockOf(productId: ProductId): Stock =
        jpaRepo.findByProductId(productId.id.value)?.toDomain() ?: Stock.noneOf(productId)

    @Transactional(readOnly = true)
    override fun findAll() = Stocks(jpaRepo.findAll().map { it.toDomain() })

    // The row of the product is updated, or created with its first stock
    override fun save(stock: Stock) {
        val storedStock = jpaRepo.findByProductId(stock.productId.id.value)
        val stockEntity = storedStock?.withQuantityOf(stock) ?: stock.toEntity()
        jpaRepo.save(stockEntity)
    }

    private fun StockEntity.withQuantityOf(stock: Stock): StockEntity {
        val storedCreatedAt = createdAt
        return copy(quantity = stock.quantity.value).also { it.createdAt = storedCreatedAt }
    }

    private fun Stock.toEntity() = StockEntity(Identity.nextId().value, productId.id.value, quantity.value)

    private fun StockEntity.toDomain() = Stock(ProductId(Identity(productId)), StockQuantity(quantity))
}
