package org.flexstore.domain.order

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.ShopUrl
import org.junit.jupiter.api.Test

class OrderPagesTest : WithAssertions {

    private val shopUrl = ShopUrl("https://flexstore.example/")
    private val orderId = OrderId("order-1")

    @Test
    fun `the page of an order should be a page of the shop`() {
        // Act
        val orderPage = OrderPage.of(shopUrl, orderId)
        // Assert
        assertThat(orderPage).isEqualTo(OrderPage("https://flexstore.example/orders/order-1"))
    }

    @Test
    fun `the confirmation page of an order should be a page of the shop`() {
        // Act
        val confirmationPage = OrderConfirmationPage.of(shopUrl, orderId)
        // Assert
        val expectedUrl = "https://flexstore.example/orders/order-1/confirmation"
        assertThat(confirmationPage).isEqualTo(OrderConfirmationPage(expectedUrl))
    }
}
