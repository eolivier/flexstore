package org.flexstore.domain.usecase.basket

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.valueobject.*
import org.junit.jupiter.api.Test
import org.ucop.domain.entity.Name
import java.math.BigDecimal

class CartTest : WithAssertions {

    @Test
    fun `addItem should add multiple items to the list`() {
        // given
        val item1 = createItem1()
        val item2 = createItem2()
        val cart = Cart()
        // when
        cart.addOrReplaceItem(item1)
        cart.addOrReplaceItem(item2)
        // then
        val items = cart.getItems()
        assertThat(items).containsExactly(item1, item2)
    }

    @Test
    fun `addItem should not add the same item twice`() {
        // given
        val item1 = createItem1()
        val cart = Cart()
        // when
        cart.addOrReplaceItem(item1)
        cart.addOrReplaceItem(item1)
        // then
        val items = cart.getItems()
        assertThat(items).containsExactly(item1)
    }

    @Test
    fun `should remove item`() {
        // given
        val item1 = createItem1()
        val cart = Cart()
        cart.addOrReplaceItem(item1)
        // when
        cart.removeItem(item1)
        // then
        assertThat(cart.getItems()).isEmpty()
    }

    @Test
    fun `should change quantity`() {
        // given
        val item1 = createItem1()
        val cart = Cart()
        cart.addOrReplaceItem(item1)
        val newQuantity = Quantity(12)
        // when
        cart.changeQuantity(item1.itemId, newQuantity)
        // then
        assertThat(cart.getItems()[0].quantity).isEqualTo(newQuantity)
    }

    private fun createItem1(): Item {
        val price1 = Price(Amount(BigDecimal(10)), Currency.EUR)
        val product1 = Product(ProductId(Identity("1")), Name("Product1"), price1)
        return Item(ItemId(Identity("1")), product1, Quantity(1))
    }

    private fun createItem2(): Item {
        val price2 = Price(Amount(BigDecimal(11)), Currency.EUR)
        val product2 = Product(ProductId(Identity("2")), Name("Product2"), price2)
        return Item(ItemId(Identity("2")), product2, Quantity(1))
    }
}