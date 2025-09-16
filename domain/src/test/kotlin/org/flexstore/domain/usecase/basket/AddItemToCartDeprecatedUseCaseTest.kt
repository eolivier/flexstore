package org.flexstore.domain.usecase.basket

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryItemRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy
import java.math.BigDecimal

private const val ITEM_ID = "item1"

class AddItemToCartDeprecatedUseCaseTest {

    @Test
    fun `should add one item to basket`() {
        // given
        val price = Price(Amount(BigDecimal(10)), Currency.EUR)
        val product = Product(ProductId(Identity("product1")), Name("product1"), price)
        val newItem = NewItem(product, Quantity(10))
        val quantityToAdd = Quantity(1)
        val itemRepository = spy(InMemoryItemRepository())
        itemRepository.clear()
        val currentCart = Cart(itemRepository)
        val addItemToBasketUseCase = AddItemToBasketUseCase(currentCart)
        // when
        addItemToBasketUseCase.perform(newItem, quantityToAdd)
        // then
        val items = currentCart.getItems()
        assertThat(items).hasSize(1)
    }

    // TODO : add scenario modify quantity where item is unknown

}