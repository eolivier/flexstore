package org.flexstore.domain.wishlist

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.Actor
import org.ucop.domain.PostConditionException
import java.time.Instant

data class WishlistItemId(override val id: Identity) : Identifiable() {
    companion object {
        fun nextId() = WishlistItemId(Identity.nextId())
    }
}

// The moment a product was saved to a wishlist
data class SavedAt(val value: Instant) {
    companion object {
        fun now() = SavedAt(InstantProvider.now())
    }
}

// A product a customer saved to their wishlist, to buy it back later, and when
data class WishlistItem(
    val wishlistItemId: WishlistItemId,
    val customer: UserId.ValidUserId,
    val productId: ProductId,
    val savedAt: SavedAt
)

// A product a customer wants to save to, or remove from, their wishlist
data class WishlistEntry(val customer: Actor, val productId: ProductId)

data class WishlistUpdateFailed(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
