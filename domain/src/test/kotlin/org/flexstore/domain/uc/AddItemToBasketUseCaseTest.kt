package org.flexstore.domain.uc

import org.flexstore.domain.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AddItemToBasketUseCaseTest {

    @Test
    fun `should add one item to basket`() {
        // given
        val itemId = ItemId(Identity("item1"))
        val price = Price(Amount(BigDecimal(10)), Currency.EUR)
        val product = Product(ProductId(Identity("product1")), Name("product1"), price)
        val item = Item(itemId, product, Quantity(10))
        val quantityToAdd = Quantity(1)
        val currentBasket = Basket()
        currentBasket.addOrReplaceItem(item)
        val addItemToBasketUseCase = AddItemToBasketUseCase(currentBasket)
        // when
        addItemToBasketUseCase.execute(item, quantityToAdd)
    }

    // TODO : add scenario modify quantity where item is unknown

}