package org.flexstore.domain.cart

import org.flexstore.domain.discount.Percentage
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class ItemsTest : WithAssertions {

    private val lamp = DefinedProduct(
        ProductId(Identity("lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR)
    )
    private val mug = DefinedProduct(
        ProductId(Identity("mug")), Name("Mug"), Price(Amount(BigDecimal(12)), Currency.EUR)
    )
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
    fun `should know the quantity of a stored item by its id`() {
        // Arrange
        val cartItems = CartItems(storedLamp)
        // Act
        val quantity = cartItems.quantityOf(ItemId(Identity("item-1")))
        // Assert
        assertThat(quantity).isEqualTo(storedLamp.quantity)
    }

    @Test
    fun `an item that is not stored should have no quantity`() {
        // Arrange
        val cartItems = CartItems(storedLamp)
        // Act
        val unknownItemId = ItemId(Identity("unknown"))
        // Assert
        assertThat(cartItems.contains(unknownItemId)).isFalse()
        assertThat(cartItems.doesNotContain(unknownItemId)).isTrue()
        assertThat(cartItems.quantityOf(unknownItemId)).isEqualTo(Quantity.none())
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

    @Test
    fun `the total price should be the price of the products on promotion`() {
        // Arrange
        val period = PromotionPeriod(PromotionStart(Instant.EPOCH), PromotionEnd(Instant.MAX))
        val halfPrice = PercentageOffPromotion(Percentage(BigDecimal(50)), period)
        val lampOnPromotion = lamp.copy(pricing = ProductPricing(lamp.price, VatRate.STANDARD, halfPrice))
        val items = Items(DraftItem(lampOnPromotion, Quantity(2)), DraftItem(mug, Quantity(1)))
        // Act
        val totalPrice = items.totalPrice()
        // Assert
        assertThat(totalPrice.amount.value).isEqualByComparingTo("47")
    }

    @Test
    fun `should contain a product that has an item`() {
        // Arrange
        val items = Items(storedLamp, DraftItem(mug, Quantity(1)))
        // Act
        val unknownProductId = ProductId(Identity("unknown"))
        // Assert
        assertThat(items.contains(mug.productId)).isTrue()
        assertThat(items.doesNotContain(mug.productId)).isFalse()
        assertThat(items.doesNotContain(unknownProductId)).isTrue()
    }

    @Test
    fun `an item of a product that is not there should be no item`() {
        // Arrange
        val items = Items(storedLamp)
        // Act
        val item = items.itemOf(mug.productId)
        // Assert
        assertThat(item).isEqualTo(NoItem)
    }

    @Test
    fun `should have at least the quantity of a product only when the product is there`() {
        // Arrange
        val items = Items(storedLamp)
        val lampQuantity = storedLamp.quantity
        // Assert
        assertThat(items.hasAtLeast(storedLamp.product.productId, lampQuantity)).isTrue()
        assertThat(items.hasAtLeast(storedLamp.product.productId, lampQuantity.increase())).isFalse()
        assertThat(items.hasAtLeast(mug.productId, Quantity.none())).isFalse()
    }
}
