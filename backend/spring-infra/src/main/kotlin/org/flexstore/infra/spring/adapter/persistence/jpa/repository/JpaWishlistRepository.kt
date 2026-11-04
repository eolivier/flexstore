package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.product.Products
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.wishlist.WishlistItem
import org.flexstore.domain.wishlist.WishlistRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.WishlistItemEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaWishlistItemRepository : JpaRepository<WishlistItemEntity, String> {
    fun findAllByUser_Id(userId: String, sort: Sort): List<WishlistItemEntity>

    fun existsByUser_IdAndProduct_Id(userId: String, productId: String): Boolean

    fun deleteByUser_IdAndProduct_Id(userId: String, productId: String)
}

// Newest first
private val WISHLIST_ORDER = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))

// The wishlists of the customers, kept in PostgreSQL: a wishlist item has no business date of its own, the
// moment it was saved is the moment its row was created; the product is read from the catalog
@Repository
@Transactional
class PostgresWishlistRepositoryAdapter(
    private val jpaRepo: JpaWishlistItemRepository,
    private val userRepo: JpaUserRepository,
    private val productRepo: JpaProductRepository
) : WishlistRepository {

    override fun save(wishlistItem: WishlistItem) {
        jpaRepo.save(wishlistItem.toEntity())
    }

    override fun remove(customer: UserId.ValidUserId, productId: ProductId) {
        jpaRepo.deleteByUser_IdAndProduct_Id(customer.value, productId.id.value)
    }

    @Transactional(readOnly = true)
    override fun contains(customer: UserId.ValidUserId, productId: ProductId) =
        jpaRepo.existsByUser_IdAndProduct_Id(customer.value, productId.id.value)

    @Transactional(readOnly = true)
    override fun findAllBy(customer: UserId.ValidUserId): Products =
        Products(jpaRepo.findAllByUser_Id(customer.value, WISHLIST_ORDER).map { it.product.toDomain() })

    private fun WishlistItem.toEntity() = WishlistItemEntity(
        id = wishlistItemId.value,
        user = userRepo.getReferenceById(customer.value),
        product = productRepo.getReferenceById(productId.id.value)
    )
}
