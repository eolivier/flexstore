package org.flexstore.domain.usecase.basket

import org.flexstore.domain.entity.Cart
import org.flexstore.domain.valueobject.*
import org.junit.jupiter.api.Test
import org.ucop.domain.entity.Name
import java.math.BigDecimal

class AddItemToCartDeprecatedUseCaseTest {

    @Test
    fun `should add one item to basket`() {
        // given
        val itemId = ItemId(Identity("item1"))
        val price = Price(Amount(BigDecimal(10)), Currency.EUR)
        val product = Product(ProductId(Identity("product1")), Name("product1"), price)
        val item = Item(itemId, product, Quantity(10))
        val quantityToAdd = Quantity(1)
        val currentCart = Cart()
        currentCart.addOrReplaceItem(item)
        val addItemToBasketUseCase = AddItemToBasketUseCase(currentCart)
        // when
        addItemToBasketUseCase.perform(item, quantityToAdd)
    }

    // TODO : add scenario modify quantity where item is unknown

}