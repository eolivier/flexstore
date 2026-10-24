package org.flexstore.domain.usecase.basket

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.entity.InvalidQuantityException
import org.flexstore.domain.valueobject.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

// The Cart aggregate alone: no repository
class CartTest : WithAssertions {

    private val product1 = DefinedProduct(ProductId(Identity("productId-1")), Name("Product1"), Price(Amount(BigDecimal(10)), Currency.EUR))
    private val product2 = DefinedProduct(ProductId(Identity("productId-2")), Name("Product2"), Price(Amount(BigDecimal(11)), Currency.EUR))
    private val item1 = CartItem(ItemId(Identity("item-1")), product1, Quantity(1))

    @Test
    fun `should add new products as draft items`() {
        val cart = Cart().add(product1, Quantity(1)).add(product2, Quantity(2))

        assertThat(cart.items).containsExactly(DraftItem(product1, Quantity(1)), DraftItem(product2, Quantity(2)))
    }

    @Test
    fun `adding a product already in the cart should change the quantity of its item`() {
        val cart = Cart(listOf(item1)).add(product1, Quantity(3))

        assertThat(cart.items).containsExactly(item1.copy(quantity = Quantity(3)))
    }

    @Test
    fun `adding a product should keep the given catalog product`() {
        val catalogProduct = product1.copy(price = Price(Amount(BigDecimal(12)), Currency.EUR))

        val cart = Cart(listOf(item1)).add(catalogProduct, Quantity(2))

        assertThat(cart.findBy(item1.itemId)?.product).isEqualTo(catalogProduct)
        assertThat(cart.toItems().totalPrice().amount.value).isEqualByComparingTo("24")
    }

    @Test
    fun `should not add a product without a positive quantity`() {
        assertThatThrownBy { Cart().add(product1, Quantity(0)) }
            .isInstanceOf(InvalidQuantityException::class.java)
    }

    @Test
    fun `should change the quantity of an item`() {
        val cart = Cart(listOf(item1)).changeQuantity(item1.itemId, Quantity(12))

        assertThat(cart.findBy(item1.itemId)?.quantity).isEqualTo(Quantity(12))
    }

    @Test
    fun `an item whose quantity becomes zero or negative should be removed`() {
        assertThat(Cart(listOf(item1)).changeQuantity(item1.itemId, Quantity(0)).items).isEmpty()
        assertThat(Cart(listOf(item1)).changeQuantity(item1.itemId, Quantity(-1)).items).isEmpty()
    }

    @Test
    fun `changing the quantity of an unknown item should leave the cart unchanged`() {
        val cart = Cart(listOf(item1))

        assertThat(cart.changeQuantity(ItemId(Identity("unknown")), Quantity(5))).isEqualTo(cart)
    }

    @Test
    fun `should remove an item`() {
        assertThat(Cart(listOf(item1)).remove(item1.itemId).items).isEmpty()
    }
}
