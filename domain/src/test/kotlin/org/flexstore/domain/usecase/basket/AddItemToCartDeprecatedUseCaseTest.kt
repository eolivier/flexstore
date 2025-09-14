package org.flexstore.domain.usecase.basket

import org.flexstore.domain.entity.Cart
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryItemRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy
import org.ucop.domain.entity.Name
import java.math.BigDecimal

private const val ITEM_ID = "item1"

class AddItemToCartDeprecatedUseCaseTest {

    @Test
    fun `should add one item to basket`() {
        // given
        val itemId = ItemId(Identity(ITEM_ID))
        val price = Price(Amount(BigDecimal(10)), Currency.EUR)
        val product = Product(ProductId(Identity("product1")), Name("product1"), price)
        val itemToAdd = OneItem(itemId, product, Quantity(10))
        val quantityToAdd = Quantity(1)
        val itemRepository = spy(InMemoryItemRepository())
        itemRepository.clear()
        val currentCart = Cart(itemRepository)
        val addItemToBasketUseCase = AddItemToBasketUseCase(currentCart)
        // when
        addItemToBasketUseCase.perform(itemToAdd, quantityToAdd)
        // then
        val items = currentCart.getItems()
        items.contains(itemToAdd)
    }

    // TODO : add scenario modify quantity where item is unknown

}