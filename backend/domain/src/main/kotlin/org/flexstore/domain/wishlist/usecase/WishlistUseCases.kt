package org.flexstore.domain.wishlist.usecase

import org.flexstore.domain.product.model.ProductId
import org.ucop.domain.entity.Actor
import org.flexstore.domain.wishlist.WishlistEntry

// The use cases of the wishlists of the customers
class WishlistUseCases(
    private val addProductToWishlistUseCase: AddProductToWishlistUseCase,
    private val removeProductFromWishlistUseCase: RemoveProductFromWishlistUseCase,
    private val listWishlistUseCase: ListWishlistUseCase
) {
    fun add(customer: Actor, productId: ProductId) =
        addProductToWishlistUseCase.unfold(WishlistEntry(customer, productId))

    fun remove(customer: Actor, productId: ProductId) =
        removeProductFromWishlistUseCase.unfold(WishlistEntry(customer, productId))

    fun getWishlist(customer: Actor) = listWishlistUseCase.unfold(customer)
}
