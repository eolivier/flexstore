package org.flexstore.domain.order

import org.flexstore.domain.cart.model.*
import org.flexstore.domain.user.model.*
import java.time.Instant

class OrderUseCaseFacade(private val placeOrderUseCase: PlaceOrderUseCase,
                         private val readOrdersUseCase: ReadOrdersUseCase) {

    // The use cases are shared by all the requests: the placed order is read back with its id
    fun placeOrder(customer: CartOwner.Customer): Order {
        val orderId = OrderId.nextId()
        placeOrderUseCase.unfold(OrderPlacement(orderId, customer, Instant.now()))
        return readOrdersUseCase.getOrder(customer.userId, orderId)
    }

    fun getOrders(customer: UserId.ValidUserId) = readOrdersUseCase.getOrders(customer)

    fun getOrder(customer: UserId.ValidUserId, orderId: OrderId) = readOrdersUseCase.getOrder(customer, orderId)
}
