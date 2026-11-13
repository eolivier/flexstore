package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.review.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductReviewEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaProductReviewRepository : JpaRepository<ProductReviewEntity, String> {
    fun findByProductId(productId: String): List<ProductReviewEntity>

    fun existsByUser_IdAndProductId(userId: String, productId: String): Boolean
}
