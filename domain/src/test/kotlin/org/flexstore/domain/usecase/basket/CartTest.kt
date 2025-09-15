package org.flexstore.domain.usecase.basket

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryItemRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

private const val ITEM_ID = "1"

class CartTest : WithAssertions {

    @Test
    fun `addItem should add multiple items to the list`() {
        // given
        val item1 = createItem1()
        val item2 = createItem2()
        val cart = Cart(InMemoryItemRepository())
        // when
        cart.addOrReplaceItem(item1)
        cart.addOrReplaceItem(item2)
        // then
        val items = cart.getItems()
        assertThat(items).containsOnlyOnceElementsOf(listOf(item1, item2))
    }

    @Test
    fun `addItem should not add the same item twice`() {
        // given
        val item1 = createItem1()
        val cart = Cart(InMemoryItemRepository())
        val initialSize = cart.getItems().size
        // when
        cart.addOrReplaceItem(item1)
        cart.addOrReplaceItem(item1)
        // then
        val items = cart.getItems()
        assertThat(items).hasSize(initialSize + 1)
        assertThat(items).containsOnlyOnce(item1)
    }

    @Test
    fun `should remove item`() {
        // given
        val item1 = createItem1()
        val cart = Cart(InMemoryItemRepository())
        cart.addOrReplaceItem(item1)
        val initialSize = cart.getItems().size
        // when
        cart.removeItem(item1)
        // then
        assertThat(cart.getItems()).hasSize(initialSize - 1)
    }

    @Test
    fun `should change quantity`() {
        // given
        val item1 = createItem1()
        val cart = Cart(InMemoryItemRepository())
        cart.addOrReplaceItem(item1)
        val newQuantity = Quantity(12)
        // when
        cart.changeQuantity(item1, newQuantity)
        // then
        val itemFound = cart.getItems().find { it is OneItem && it.itemId.id.value == ITEM_ID } as OneItem
        assertThat(itemFound).isNotNull
        assertThat(itemFound.quantity).isEqualTo(newQuantity)
    }

    private fun createItem1(): Item {
        val price1 = Price(Amount(BigDecimal(10)), Currency.EUR)
        val product1 = Product(ProductId(Identity(ITEM_ID)), Name("Product1"), price1)
        return OneItem(ItemId(Identity(ITEM_ID)), product1, Quantity(1))
    }

    private fun createItem2(): Item {
        val price2 = Price(Amount(BigDecimal(11)), Currency.EUR)
        val product2 = Product(ProductId(Identity("2")), Name("Product2"), price2)
        return OneItem(ItemId(Identity("2")), product2, Quantity(1))
    }
}