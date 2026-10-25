package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.settings.ReadShopSettingsUseCase
import org.flexstore.domain.user.model.*
import java.time.Instant

// The use cases of the orders: placing an order, reading it (with the delay to pay it), confirming it to its
// customer
class OrderUseCases(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val readOrdersUseCase: ReadOrdersUseCase,
    private val sendOrderConfirmationEmailUseCase: SendOrderConfirmationEmailUseCase,
    private val readShopSettingsUseCase: ReadShopSettingsUseCase
) {

    // The use cases are shared by all the requests: the placed order is read back with its id.
    // It awaits its payment, which confirms it to the customer by email (see PaymentService)
    fun placeOrder(checkout: Checkout): Order {
        val orderStamp = OrderStamp(OrderId.nextId(), Instant.now())
        val orderPlacement = OrderPlacement(orderStamp, checkout)
        placeOrderUseCase.unfold(orderPlacement)
        return readOrdersUseCase.getOrder(checkout.customer.userId, orderStamp.orderId)
    }

    fun getOrders(customer: UserId.ValidUserId) = readOrdersUseCase.getOrders(customer)

    fun getOrder(customer: UserId.ValidUserId, orderId: OrderId) = readOrdersUseCase.getOrder(customer, orderId)

    // Any order, whoever its customer (the payment provider confirms it)
    fun getAnyOrder(orderId: OrderId) = readOrdersUseCase.getAnyOrder(orderId)

    // The delay of the shop settings, which gives the payment deadline of an order
    fun paymentDelay() = readShopSettingsUseCase.getSettings().paymentDelay

    fun sendConfirmationOf(order: Order) = sendOrderConfirmationEmailUseCase.unfold(order)
}
