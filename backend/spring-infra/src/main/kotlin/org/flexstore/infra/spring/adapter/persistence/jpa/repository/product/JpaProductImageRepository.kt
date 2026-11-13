package org.flexstore.infra.spring.adapter.persistence.jpa.repository.product

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductImageEntity
import org.flexstore.domain.image.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface JpaProductImageRepository : JpaRepository<ProductImageEntity, String> {
    fun findByProductIdAndFormat(productId: String, format: String): ProductImageEntity?

    @Query("SELECT DISTINCT i.productId FROM ProductImageEntity i")
    fun findProductIds(): List<String>

    @Modifying
    fun deleteAllByProductId(productId: String)
}
