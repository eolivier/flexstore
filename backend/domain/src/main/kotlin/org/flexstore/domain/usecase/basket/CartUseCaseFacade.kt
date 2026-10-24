package org.flexstore.domain.usecase.basket

class CartUseCaseFacade(private val addItemToCartUseCase: AddItemToCartUseCase,
                        private val changeCartItemQuantityUseCase: ChangeCartItemQuantityUseCase,
                        private val readCartUseCase: ReadCartUseCase) {

    fun addItem(request: AddItemToCartRequest) = addItemToCartUseCase.unfold(request)

    fun changeQuantity(request: ChangeCartItemQuantityRequest) = changeCartItemQuantityUseCase.unfold(request)

    fun getCart() = readCartUseCase.getCart()
}
