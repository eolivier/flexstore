package org.flexstore.domain.wishlist

import org.flexstore.domain.product.Products
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.user.model.UserId

// What the customers saved to their wishlist
interface WishlistRepository {

    fun save(wishlistItem: WishlistItem)

    fun remove(customer: UserId.ValidUserId, productId: ProductId)

    // The product is already saved to the wishlist of the customer
    fun contains(customer: UserId.ValidUserId, productId: ProductId): Boolean

    fun doesNotContain(customer: UserId.ValidUserId, productId: ProductId) = !contains(customer, productId)

    // The products the customer saved, newest first, with enough data to render them like the catalog
    fun findAllBy(customer: UserId.ValidUserId): Products
}
