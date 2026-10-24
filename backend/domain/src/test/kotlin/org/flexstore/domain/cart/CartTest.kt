package org.flexstore.domain.cart

import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal

// The Cart aggregate alone: no repository
class CartTest : WithAssertions {

    private val product1 = DefinedProduct(ProductId(Identity("productId-1")), Name("Product1"), Price(Amount(BigDecimal(10)), Currency.EUR))
    private val product2 = DefinedProduct(ProductId(Identity("productId-2")), Name("Product2"), Price(Amount(BigDecimal(11)), Currency.EUR))
    private val item1 = CartItem(ItemId(Identity("item-1")), product1, Quantity(1))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val visitor = CartOwner.Visitor(VisitorCartId.nextId())

    @Test
    fun `should add new products as draft items`() {
        // Arrange
        val cart = Cart(customer).add(product1, Quantity(1))
        // Act
        val updatedCart = cart.add(product2, Quantity(2))
        // Assert
        assertThat(updatedCart.items.asList()).containsExactly(DraftItem(product1, Quantity(1)), DraftItem(product2, Quantity(2)))
    }

    @Test
    fun `adding a product already in the cart should change the quantity of its item`() {
        // Arrange
        val cart = Cart(customer, item1)
        // Act
        val updatedCart = cart.add(product1, Quantity(3))
        // Assert
        assertThat(updatedCart.items.asList()).containsExactly(item1.copy(quantity = Quantity(3)))
    }

    @Test
    fun `adding a product should keep the given catalog product`() {
        // Arrange
        val cart = Cart(customer, item1)
        val catalogProduct = product1.copy(price = Price(Amount(BigDecimal(12)), Currency.EUR))
        // Act
        val updatedCart = cart.add(catalogProduct, Quantity(2))
        // Assert
        assertThat(updatedCart.findBy(item1.itemId)?.product).isEqualTo(catalogProduct)
        assertThat(updatedCart.items.totalPrice().amount.value).isEqualByComparingTo("24")
    }

    @ParameterizedTest(name = "a quantity of {0} is rejected")
    @ValueSource(ints = [0, -1])
    fun `should not add a product without a positive quantity`(quantity: Int) {
        // Arrange
        val cart = Cart(customer)
        // Act
        val thrown = catchThrowable { cart.add(product1, Quantity(quantity)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidQuantityException::class.java)
    }

    @Test
    fun `should change the quantity of an item`() {
        // Arrange
        val cart = Cart(customer, item1)
        // Act
        val updatedCart = cart.changeQuantity(item1.itemId, Quantity(12))
        // Assert
        assertThat(updatedCart.findBy(item1.itemId)?.quantity).isEqualTo(Quantity(12))
    }

    @ParameterizedTest(name = "a quantity of {0} removes the item")
    @ValueSource(ints = [0, -1, -10])
    fun `an item whose quantity becomes zero or negative should be removed`(quantity: Int) {
        // Arrange
        val cart = Cart(customer, item1)
        // Act
        val updatedCart = cart.changeQuantity(item1.itemId, Quantity(quantity))
        // Assert
        assertThat(updatedCart.isEmpty()).isTrue()
    }

    @Test
    fun `changing the quantity of an unknown item should leave the cart unchanged`() {
        // Arrange
        val cart = Cart(customer, item1)
        // Act
        val updatedCart = cart.changeQuantity(ItemId(Identity("unknown")), Quantity(5))
        // Assert
        assertThat(updatedCart).isEqualTo(cart)
    }

    @Test
    fun `should remove an item`() {
        // Arrange
        val cart = Cart(customer, item1)
        // Act
        val updatedCart = cart.remove(item1.itemId)
        // Assert
        assertThat(updatedCart.isEmpty()).isTrue()
    }

    @Test
    fun `merging should add the items of the other cart`() {
        // Arrange
        val customerCart = Cart(customer, item1)
        val visitorCart = Cart(visitor, CartItem(ItemId(Identity("item-2")), product2, Quantity(2)))
        // Act
        val mergedCart = customerCart.mergeWith(visitorCart)
        // Assert
        assertThat(mergedCart.owner).isEqualTo(customer)
        assertThat(mergedCart.items.asList()).containsExactly(item1, DraftItem(product2, Quantity(2)))
    }

    @ParameterizedTest(name = "customer {0}, visitor {1}: the merged cart has {2}")
    @CsvSource("1, 3, 3", "4, 1, 4", "2, 2, 2")
    fun `merging should keep the larger quantity of a product in both carts`(customerQuantity: Int, visitorQuantity: Int, mergedQuantity: Int) {
        // Arrange
        val customerCart = Cart(customer, item1.copy(quantity = Quantity(customerQuantity)))
        val visitorCart = Cart(visitor, CartItem(ItemId(Identity("item-2")), product1, Quantity(visitorQuantity)))
        // Act
        val mergedCart = customerCart.mergeWith(visitorCart)
        // Assert
        assertThat(mergedCart.items.asList()).containsExactly(item1.copy(quantity = Quantity(mergedQuantity)))
    }

    @Test
    fun `merging an empty cart should leave the cart unchanged`() {
        // Arrange
        val customerCart = Cart(customer, item1)
        // Act
        val mergedCart = customerCart.mergeWith(Cart(visitor))
        // Assert
        assertThat(mergedCart).isEqualTo(customerCart)
    }

    @Test
    fun `a visitor cart id should be a UUID`() {
        // Act
        val thrown = catchThrowable { VisitorCartId("not-a-uuid") }
        // Assert
        assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
    }
}
