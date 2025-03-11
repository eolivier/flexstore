package org.flexstore.domain

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.entity.Basket
import org.junit.jupiter.api.Test
import org.ucop.domain.entity.Name
import java.math.BigDecimal

class BasketTest : WithAssertions {

    @Test
    fun `addItem should add multiple items to the list`() {
        // given
        val item1 = createItem1()
        val item2 = createItem2()
        val basket = Basket()
        // when
        basket.addOrReplaceItem(item1)
        basket.addOrReplaceItem(item2)
        // then
        val items = basket.getItems()
        assertThat(items).containsExactly(item1, item2)
    }

    @Test
    fun `addItem should not add the same item twice`() {
        // given
        val item1 = createItem1()
        val basket = Basket()
        // when
        basket.addOrReplaceItem(item1)
        basket.addOrReplaceItem(item1)
        // then
        val items = basket.getItems()
        assertThat(items).containsExactly(item1)
    }

    @Test
    fun `should remove item`() {
        // given
        val item1 = createItem1()
        val basket = Basket()
        basket.addOrReplaceItem(item1)
        // when
        basket.removeItem(item1)
        // then
        assertThat(basket.getItems()).isEmpty()
    }

    @Test
    fun `should change quantity`() {
        // given
        val item1 = createItem1()
        val basket = Basket()
        basket.addOrReplaceItem(item1)
        val newQuantity = Quantity(12)
        // when
        basket.changeQuantity(item1.itemId, newQuantity)
        // then
        assertThat(basket.getItems()[0].quantity).isEqualTo(newQuantity)
    }

    private fun createItem1(): Item {
        val price1 = Price(Amount(BigDecimal(10)), Currency.EUR)
        val product1 = Product(ProductId(Identity("1")), Name("Product1"), price1)
        return Item(ItemId(Identity("1")), product1, Quantity(1))
    }

    private fun createItem2(): Item {
        val price2 = Price(Amount(BigDecimal(11)), Currency.EUR)
        val product2 = Product(ProductId(Identity("2")), Name("Product2"), price2)
        return Item(ItemId(Identity("2")), product2,Quantity(1))
    }
}