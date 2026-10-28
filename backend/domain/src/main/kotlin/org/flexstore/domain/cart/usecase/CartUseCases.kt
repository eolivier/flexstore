package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*

// The use cases of the carts
class CartUseCases(private val addItemToCartUseCase: AddItemToCartUseCase,
                        private val changeCartItemQuantityUseCase: ChangeCartItemQuantityUseCase,
                        private val readCartUseCase: ReadCartUseCase,
                        private val mergeCartsUseCase: MergeCartsUseCase) {

    fun addItem(request: AddItemToCartRequest) = addItemToCartUseCase.unfold(request)

    fun changeQuantity(request: ChangeCartItemQuantityRequest) = changeCartItemQuantityUseCase.unfold(request)

    fun getCart(owner: CartOwner) = readCartUseCase.unfold(owner)

    fun mergeCarts(request: MergeCartsRequest) = mergeCartsUseCase.unfold(request)
}
