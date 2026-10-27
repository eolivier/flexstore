package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.StockEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.infra.spring.adapter.persistence.jpa.toEntity
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.Quantity
import org.flexstore.domain.stock.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaStockRepository : JpaRepository<StockEntity, String> {
    fun findByProductId(productId: String): StockEntity?

    // One statement: the database checks and takes the units together, one order after the other. The update
    // date is set here, as Hibernate does not set it on a bulk update. The number of rows updated: 0 or 1
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "UPDATE StockEntity s SET s.quantity = s.quantity - :quantity, s.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE s.productId = :productId AND s.quantity >= :quantity"
    )
    fun takeOut(productId: String, quantity: Int): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "UPDATE StockEntity s SET s.quantity = s.quantity + :quantity, s.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE s.productId = :productId"
    )
    fun putBack(productId: String, quantity: Int): Int
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

    override fun reserve(productId: ProductId, quantity: Quantity): StockReservation {
        val updatedRows = jpaRepo.takeOut(productId.id.value, quantity.value)
        return if (updatedRows == 1) StockReservation.RESERVED else StockReservation.NOT_COVERED
    }

    override fun release(productId: ProductId, quantity: Quantity) {
        jpaRepo.putBack(productId.id.value, quantity.value)
    }

    private fun StockEntity.withQuantityOf(stock: Stock): StockEntity {
        val storedCreatedAt = createdAt
        return copy(quantity = stock.quantity.value).also { it.createdAt = storedCreatedAt }
    }

    private fun Stock.toEntity() = StockEntity(Identity.nextId().value, productId.id.value, quantity.value)

    private fun StockEntity.toDomain() = Stock(ProductId(Identity(productId)), StockQuantity(quantity))
}
