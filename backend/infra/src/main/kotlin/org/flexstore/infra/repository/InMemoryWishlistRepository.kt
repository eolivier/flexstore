package org.flexstore.infra.repository

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.product.ProductRepository
import org.flexstore.domain.product.model.Products
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.wishlist.WishlistItem
import org.flexstore.domain.wishlist.WishlistRepository

// The wishlists in memory: for the tests. The product of each item is read from the catalog, like the adapter
class InMemoryWishlistRepository(private val productRepository: ProductRepository) : WishlistRepository {

    private val wishlistItems = mutableListOf<WishlistItem>()

    override fun save(wishlistItem: WishlistItem) {
        wishlistItems.add(wishlistItem)
    }

    override fun remove(customer: UserId.ValidUserId, productId: ProductId) {
        wishlistItems.removeIf { it.customer == customer && it.productId == productId }
    }

    override fun contains(customer: UserId.ValidUserId, productId: ProductId) =
        wishlistItems.any { it.customer == customer && it.productId == productId }

    override fun findAllBy(customer: UserId.ValidUserId): Products {
        val items = wishlistItems.filter { it.customer == customer }.sortedByDescending { it.savedAt.value }
        return Products(items.mapNotNull { productRepository.findById(it.productId) })
    }
}
