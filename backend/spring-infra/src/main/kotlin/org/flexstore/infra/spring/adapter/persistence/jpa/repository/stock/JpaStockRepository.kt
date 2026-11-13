package org.flexstore.infra.spring.adapter.persistence.jpa.repository.stock

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductEntity
import org.flexstore.domain.stock.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

// The stock is two columns of the product table, written by these statements only (the product entity does not
// update them). The update date is set here, as Hibernate does not set it on a bulk update. Each update returns
// the number of rows updated: 0 or 1
@Repository
interface JpaStockRepository : JpaRepository<ProductEntity, String> {

    @Query(
        "SELECT new org.flexstore.infra.spring.adapter.persistence.jpa.repository.stock.StockRow(" +
            "p.id, p.stockQuantity, p.stockAlertThreshold) FROM ProductEntity p WHERE p.id = :productId"
    )
    fun findStockOf(productId: String): StockRow?

    // Only the products whose stock was given
    @Query(
        "SELECT new org.flexstore.infra.spring.adapter.persistence.jpa.repository.stock.StockRow(" +
            "p.id, p.stockQuantity, p.stockAlertThreshold) FROM ProductEntity p " +
            "WHERE p.stockQuantity IS NOT NULL"
    )
    fun findGivenStocks(): List<StockRow>

    @Query(
        "SELECT new org.flexstore.infra.spring.adapter.persistence.jpa.repository.stock.StockRow(" +
            "p.id, p.stockQuantity, p.stockAlertThreshold) FROM ProductEntity p " +
            "WHERE p.stockQuantity <= p.stockAlertThreshold"
    )
    fun findLowStocks(): List<StockRow>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "UPDATE product SET stock_quantity = :quantity, updated_at = now() WHERE id = :productId",
        nativeQuery = true
    )
    fun giveQuantity(productId: String, quantity: Int): Int

    // A threshold given for a product that has no stock yet gives it a stock without units
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "UPDATE product SET stock_alert_threshold = :threshold, stock_quantity = COALESCE(stock_quantity, 0), " +
            "updated_at = now() WHERE id = :productId",
        nativeQuery = true
    )
    fun giveThreshold(productId: String, threshold: Int): Int

    // One statement: the database checks and takes the units together, one order after the other
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "UPDATE product SET stock_quantity = stock_quantity - :quantity, updated_at = now() " +
            "WHERE id = :productId AND stock_quantity >= :quantity",
        nativeQuery = true
    )
    fun takeOut(productId: String, quantity: Int): Int

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "UPDATE product SET stock_quantity = stock_quantity + :quantity, updated_at = now() " +
            "WHERE id = :productId",
        nativeQuery = true
    )
    fun putBack(productId: String, quantity: Int): Int
}
