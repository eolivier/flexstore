package org.flexstore.domain.order

import org.flexstore.domain.sharedkernel.ShopUrl

// The page of the shop where the customer follows an order
data class OrderPage(val url: String) {
    companion object {
        fun of(shopUrl: ShopUrl, orderId: OrderId) = OrderPage(shopUrl.pageAt("orders/${orderId.value}"))
    }
}

// The page of the shop that confirms an order to the customer, where they come back after paying it
data class OrderConfirmationPage(val url: String) {
    companion object {
        fun of(shopUrl: ShopUrl, orderId: OrderId) =
            OrderConfirmationPage(shopUrl.pageAt("orders/${orderId.value}/confirmation"))
    }
}
