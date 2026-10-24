package org.flexstore.domain.usecase.basket

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.entity.InvalidQuantityException
import org.flexstore.domain.usecase.product.ProductNotFoundException
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryItemRepository
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CartTest : WithAssertions {

    private val product1 = DefinedProduct(ProductId(Identity("productId-1")), Name("Product1"), Price(Amount(BigDecimal(10)), Currency.EUR))
    private val product2 = DefinedProduct(ProductId(Identity("productId-2")), Name("Product2"), Price(Amount(BigDecimal(11)), Currency.EUR))

    private val itemRepository = InMemoryItemRepository().apply { clear() }
    private val cart = Cart(itemRepository, InMemoryProductRepository().apply { save(product1); save(product2) })

    @Test
    fun `addItem should add multiple items to the list`() {
        // when
        cart.add(DraftItem(product1, Quantity(1)))
        cart.add(DraftItem(product2, Quantity(1)))
        // then
        assertThat(cart.getItems().items).hasSize(2)
    }

    @Test
    fun `adding a product already in the cart should change the quantity of its item`() {
        // given
        val firstItem = cart.add(DraftItem(product1, Quantity(1)))
        // when
        val secondItem = cart.add(DraftItem(product1, Quantity(3)))
        // then
        assertThat(secondItem.itemId).isEqualTo(firstItem.itemId)
        assertThat(cart.getItems().items).containsExactly(CartItem(firstItem.itemId, product1, Quantity(3)))
    }

    @Test
    fun `an item should hold the catalog product`() {
        // given
        val productSentByTheClient = product1.copy(name = Name("Fake name"), price = Price(Amount(BigDecimal.ONE), Currency.EUR))
        // when
        val item = cart.add(DraftItem(productSentByTheClient, Quantity(2)))
        // then
        assertThat(item.product).isEqualTo(product1)
        assertThat(cart.getItems().totalPrice().amount.value).isEqualByComparingTo("20")
    }

    @Test
    fun `should not add a product missing from the catalog`() {
        val unknownProduct = product1.copy(productId = ProductId(Identity("unknown")))

        assertThatThrownBy { cart.add(DraftItem(unknownProduct, Quantity(1))) }
            .isInstanceOf(ProductNotFoundException::class.java)
        assertThat(cart.getItems().items).isEmpty()
    }

    @Test
    fun `should not add an item without a positive quantity`() {
        assertThatThrownBy { cart.add(DraftItem(product1, Quantity(0))) }
            .isInstanceOf(InvalidQuantityException::class.java)
        assertThat(cart.getItems().items).isEmpty()
    }

    @Test
    fun `should remove item`() {
        // given
        val oneItem = cart.add(DraftItem(product1, Quantity(1)))
        // when
        cart.removeItem(oneItem)
        // then
        assertThat(cart.getItems().items).isEmpty()
    }

    @Test
    fun `should change quantity`() {
        // given
        val oneItem = cart.add(DraftItem(product1, Quantity(1)))
        val newQuantity = Quantity(12)
        // when
        cart.changeQuantity(oneItem, newQuantity)
        // then
        val itemFound = cart.findBy(oneItem.itemId) as CartItem
        assertThat(itemFound.quantity).isEqualTo(newQuantity)
    }

    @Test
    fun `updating an item should only change its quantity`() {
        // given
        val oneItem = cart.add(DraftItem(product1, Quantity(1)))
        // when
        cart.save(CartItem(oneItem.itemId, product1.copy(name = Name("Fake name")), Quantity(4)))
        // then
        assertThat(cart.findBy(oneItem.itemId)).isEqualTo(CartItem(oneItem.itemId, product1, Quantity(4)))
    }

    @Test
    fun `an item whose quantity becomes zero should be removed`() {
        // given
        val oneItem = cart.add(DraftItem(product1, Quantity(1)))
        // when
        cart.decreaseQuantity(oneItem)
        // then
        assertThat(cart.getItems().items).isEmpty()
    }

    @Test
    fun `updating an item already removed should do nothing`() {
        // given
        val oneItem = cart.add(DraftItem(product1, Quantity(1)))
        cart.removeItem(oneItem)
        // when
        cart.save(oneItem.copy(quantity = Quantity(5)))
        // then
        assertThat(cart.getItems().items).isEmpty()
    }
}
