package org.flexstore.domain.cart

import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ItemsTest : WithAssertions {

    private val lamp = DefinedProduct(ProductId(Identity("lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val mug = DefinedProduct(ProductId(Identity("mug")), Name("Mug"), Price(Amount(BigDecimal(12)), Currency.EUR))
    private val storedLamp = CartItem(ItemId(Identity("item-1")), lamp, Quantity(2))

    @Test
    fun `the cart items should be the stored items only`() {
        // Arrange
        val items = Items(storedLamp, DraftItem(mug, Quantity(1)))
        // Act
        val cartItems = items.cartItems()
        // Assert
        assertThat(cartItems).isEqualTo(CartItems(storedLamp))
        assertThat(cartItems.itemIds()).containsExactly(ItemId(Identity("item-1")))
    }

    @Test
    fun `should find a stored item by its id`() {
        // Arrange
        val cartItems = CartItems(storedLamp)
        // Act
        val item = cartItems.findBy(ItemId(Identity("item-1")))
        // Assert
        assertThat(item).isEqualTo(storedLamp)
    }

    @Test
    fun `the total price should include the draft items`() {
        // Arrange
        val items = Items(storedLamp, DraftItem(mug, Quantity(1)))
        // Act
        val totalPrice = items.totalPrice()
        // Assert
        assertThat(totalPrice.amount.value).isEqualByComparingTo("82")
    }
}
