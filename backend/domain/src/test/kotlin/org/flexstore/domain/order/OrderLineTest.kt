package org.flexstore.domain.order

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.discount.Percentage
import org.flexstore.domain.order.model.OrderLine
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class OrderLineTest : WithAssertions {

    @Test
    fun `the line of a product on promotion should be priced with the promotion`() {
        // Arrange
        val regularPrice = Price(Amount(BigDecimal(40)), Currency.EUR)
        val period = PromotionPeriod(PromotionStart(Instant.EPOCH), PromotionEnd(Instant.MAX))
        val promotion = PercentageOffPromotion(Percentage(BigDecimal(25)), period)
        val pricing = ProductPricing(regularPrice, VatRate.STANDARD, promotion)
        val deskLamp = DefinedProduct(ProductId("desk-lamp"), Name("Desk lamp"), regularPrice)
        val deskLampOnPromotion = deskLamp.copy(pricing = pricing)
        // Act
        val orderLine = OrderLine.of(deskLampOnPromotion, Quantity(2))
        // Assert
        assertThat(orderLine.unitPrice.amount.value).isEqualByComparingTo("30")
        assertThat(orderLine.linePrice().amount.value).isEqualByComparingTo("60")
    }
}
