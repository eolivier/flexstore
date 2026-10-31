package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.settings.ReadShopSettingsUseCase
import org.flexstore.domain.user.model.*

// The use cases of the orders: reading them (with the delay to pay them), the shipping options, confirming an
// order to its customer. Placing an order is the OrderCheckoutService
class OrderUseCases(
    private val orderQueryUseCases: OrderQueryUseCases,
    private val sendOrderConfirmationEmailUseCase: SendOrderConfirmationEmailUseCase,
    private val readShopSettingsUseCase: ReadShopSettingsUseCase,
    private val proposeShippingOptionsUseCase: ProposeShippingOptionsUseCase
) {

    fun getOrders(customer: UserId.ValidUserId) = orderQueryUseCases.listCustomerOrdersUseCase.unfold(customer)

    fun getOrder(customer: UserId.ValidUserId, orderId: OrderId) =
        orderQueryUseCases.readCustomerOrderUseCase.unfold(CustomerOrderLookup(customer, orderId))

    // Any order, whoever its customer (the payment provider confirms it)
    fun getAnyOrder(orderId: OrderId) = orderQueryUseCases.readAnyOrderUseCase.unfold(orderId)

    // The delay of the shop settings, which gives the payment deadline of an order
    fun paymentDelay() = readShopSettingsUseCase.unfold(Unit).paymentDelay

    // The shipping methods the customer chooses from, at their cost for their cart
    fun shippingOptionsFor(customer: CartOwner.Customer) = proposeShippingOptionsUseCase.unfold(customer)

    fun sendConfirmationOf(order: Order) = sendOrderConfirmationEmailUseCase.unfold(order)
}
