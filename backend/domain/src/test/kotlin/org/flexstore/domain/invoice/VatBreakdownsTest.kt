package org.flexstore.domain.invoice

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class VatBreakdownsTest : WithAssertions {

    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val lampPricing = ProductPricing(Price(Amount(BigDecimal(35)), Currency.EUR), VatRate.STANDARD)
    private val deskLamp = productAt(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPricing)
    private val bookPricing = ProductPricing(Price(Amount(BigDecimal(20)), Currency.EUR), VatRate.REDUCED)
    private val cookBook = productAt(ProductId(Identity("cook-book")), Name("Cook book"), bookPricing)

    private fun productAt(productId: ProductId, name: Name, pricing: ProductPricing): DefinedProduct {
        val translations = ProductTranslations.inDefaultLanguage(ProductText(name, Description("A product of the tests")))
        return DefinedProduct(productId, Sku.from(productId.id.value), translations, Category.HOME, pricing)
    }

    // Shipped by express (9,90 €), which is never free
    private fun orderOf(cart: Cart): Order {
        val delivery = OrderDelivery(homeOrderAddresses, ShippingMethod.EXPRESS)
        val orderStamp = OrderStamp(OrderId("order-1"), Instant.parse("2026-10-24T10:00:00Z"))
        return Order.place(OrderPlacement(orderStamp, Checkout(customer, delivery)), cart)
    }

    private fun priceOf(amount: String) = Price(Amount(BigDecimal(amount)), Currency.EUR)

    @Test
    fun `an order at a single rate should have its whole price at that rate`() {
        // Arrange
        val order = orderOf(Cart(customer).add(deskLamp, Quantity(1)))
        // Act
        val vatBreakdowns = VatBreakdowns.of(order)
        // Assert
        assertThat(vatBreakdowns.asList()).containsExactly(VatBreakdown(priceOf("44.90"), VatRate.STANDARD))
    }

    @Test
    fun `the shipping should be shared between the rates in proportion to the price of their items`() {
        // Arrange
        val order = orderOf(Cart(customer).add(deskLamp, Quantity(1)).add(cookBook, Quantity(1)))
        // Act
        val vatBreakdowns = VatBreakdowns.of(order)
        // Assert
        assertThat(vatBreakdowns.asList()).containsExactly(
            VatBreakdown(priceOf("41.30"), VatRate.STANDARD),
            VatBreakdown(priceOf("23.60"), VatRate.REDUCED)
        )
    }

    @Test
    fun `the price without VAT should add up the prices without VAT of each rate`() {
        // Arrange
        val order = orderOf(Cart(customer).add(deskLamp, Quantity(1)).add(cookBook, Quantity(1)))
        // Act
        val priceExcludingVat = VatBreakdowns.of(order).priceExcludingVat()
        // Assert
        assertThat(priceExcludingVat).isEqualTo(priceOf("56.79"))
    }

    @Test
    fun `an order line should keep the rate of VAT of its product when the order was placed`() {
        // Arrange
        val order = orderOf(Cart(customer).add(cookBook, Quantity(2)))
        // Act
        val orderLine = order.lines.asList().single()
        // Assert
        assertThat(orderLine.vatRate).isEqualTo(VatRate.REDUCED)
    }
}
