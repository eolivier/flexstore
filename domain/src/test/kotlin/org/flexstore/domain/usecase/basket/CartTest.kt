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
        val item1 = createNewItem1()
        val item2 = createNewItem2()
        val cart = Cart(InMemoryItemRepository())
        val initialSize = cart.getItems().size
        // when
        cart.add(item1)
        cart.add(item2)
        // then
        val items = cart.getItems()
        assertThat(items).hasSize(initialSize + 2)
    }

    @Test
    fun `addItem should not add the same item twice`() {
        // given
        val item1 = createNewItem1()
        val cart = Cart(InMemoryItemRepository())
        val initialSize = cart.getItems().size
        // when
        cart.add(item1)
        cart.add(item1)
        // then
        val items = cart.getItems()
        assertThat(items).hasSize(initialSize + 1)
    }

    @Test
    fun `should remove item`() {
        // given
        val newItem = createNewItem1()
        val cart = Cart(InMemoryItemRepository())
        val oneItem = cart.add(newItem)
        val initialSize = cart.getItems().size
        // when
        cart.removeItem(oneItem)
        // then
        assertThat(cart.getItems()).hasSize(initialSize - 1)
    }

    @Test
    fun `should change quantity`() {
        // given
        val newItem = createNewItem1()
        val cart = Cart(InMemoryItemRepository())
        val oneItem = cart.add(newItem)
        val newQuantity = Quantity(12)
        // when
        cart.changeQuantity(oneItem, newQuantity)
        // then
        val itemFound = cart.getItems().find { it is OneItem && it.itemId == oneItem.itemId } as OneItem
        assertThat(itemFound).isNotNull
        assertThat(itemFound.quantity).isEqualTo(newQuantity)
    }

    private fun createNewItem1(): NewItem {
        val price1 = Price(Amount(BigDecimal(10)), Currency.EUR)
        val product1 = Product(ProductId(Identity("productId-1")), Name("Product1"), price1)
        return NewItem(product1, Quantity(1))
    }

    private fun createNewItem2(): NewItem {
        val price2 = Price(Amount(BigDecimal(11)), Currency.EUR)
        val product2 = Product(ProductId(Identity("productId-2")), Name("Product2"), price2)
        return NewItem(product2, Quantity(1))
    }
}