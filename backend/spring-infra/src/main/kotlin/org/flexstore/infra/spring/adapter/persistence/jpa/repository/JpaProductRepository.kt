package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.product.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductEntity
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface JpaProductRepository : JpaRepository<ProductEntity, String> {
    fun findAllByCategory(category: String, sort: Sort): List<ProductEntity>

    fun existsBySkuAndIdNot(sku: String, id: String): Boolean

    fun existsBySku(sku: String): Boolean

    // A product of this category and price, with this name and description in the default language
    @Query(
        """SELECT COUNT(p) > 0 FROM ProductEntity p JOIN p.translations t
           WHERE t.language = :language AND t.name = :name AND t.description = :description
           AND p.category = :category AND p.price = :price AND p.currency = :currency"""
    )
    fun existsWith(
        language: String,
        name: String,
        description: String,
        category: String,
        price: BigDecimal,
        currency: String
    ): Boolean
}
