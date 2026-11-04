package org.flexstore.domain.wishlist.usecase

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.wishlist.WishlistEntry

// The use cases of the wishlists of the customers
class WishlistUseCases(
    private val addProductToWishlistUseCase: AddProductToWishlistUseCase,
    private val removeProductFromWishlistUseCase: RemoveProductFromWishlistUseCase,
    private val listWishlistUseCase: ListWishlistUseCase
) {
    fun add(customer: UserId.ValidUserId, productId: ProductId) =
        addProductToWishlistUseCase.unfold(WishlistEntry(customer, productId))

    fun remove(customer: UserId.ValidUserId, productId: ProductId) =
        removeProductFromWishlistUseCase.unfold(WishlistEntry(customer, productId))

    fun getWishlist(customer: UserId.ValidUserId) = listWishlistUseCase.unfold(customer)
}
