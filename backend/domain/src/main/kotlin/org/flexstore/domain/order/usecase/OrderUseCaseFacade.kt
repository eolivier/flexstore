package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.user.model.*
import java.time.Instant

class OrderUseCaseFacade(private val placeOrderUseCase: PlaceOrderUseCase,
                         private val readOrdersUseCase: ReadOrdersUseCase,
                         private val sendOrderConfirmationEmailUseCase: SendOrderConfirmationEmailUseCase) {

    // The use cases are shared by all the requests: the placed order is read back with its id,
    // then confirmed to the customer by email
    fun placeOrder(checkout: Checkout): Order {
        val orderStamp = OrderStamp(OrderId.nextId(), Instant.now())
        val orderPlacement = OrderPlacement(orderStamp, checkout)
        placeOrderUseCase.unfold(orderPlacement)
        val placedOrder = readOrdersUseCase.getOrder(checkout.customer.userId, orderStamp.orderId)
        sendOrderConfirmationEmailUseCase.unfold(placedOrder)
        return placedOrder
    }

    fun getOrders(customer: UserId.ValidUserId) = readOrdersUseCase.getOrders(customer)

    fun getOrder(customer: UserId.ValidUserId, orderId: OrderId) = readOrdersUseCase.getOrder(customer, orderId)
}
