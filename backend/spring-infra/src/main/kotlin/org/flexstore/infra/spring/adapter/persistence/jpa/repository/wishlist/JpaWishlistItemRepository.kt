package org.flexstore.infra.spring.adapter.persistence.jpa.repository.wishlist

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.WishlistItemEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaWishlistItemRepository : JpaRepository<WishlistItemEntity, String> {
    fun findAllByUser_Id(userId: String, sort: Sort): List<WishlistItemEntity>

    fun existsByUser_IdAndProduct_Id(userId: String, productId: String): Boolean

    fun deleteByUser_IdAndProduct_Id(userId: String, productId: String)
}
