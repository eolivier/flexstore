package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An administrator moves an order to the next status of its life cycle: paid -> processing -> shipped ->
// delivered (the payment moves an order awaiting it).
// The initial status is checked before: the order still has the status the administrator saw,
// and it has a next one. The final status is checked after: the order has the next status of the initial one
// The moment of the change is read from InstantProvider
class AdvanceOrderStatusUseCase(
    private val orderRepository: OrderRepository
) : UseCase<OrderStatusAdvance> {

    override fun getPreConditions(): PreConditions<OrderStatusAdvance> =
        PreConditions(orderExistsCondition(), initialStatusIsCurrentCondition(), initialStatusHasNextCondition())

    override fun getNominalScenario(): NominalScenario<OrderStatusAdvance> {
        val advanceStep = Step<OrderStatusAdvance> { orderStatusAdvance ->
            val order = orderRepository.findById(orderStatusAdvance.orderId)!!
            orderRepository.save(order.advancedAt(StatusChangedAt.now()))
        }
        return NominalScenario(listOf(advanceStep))
    }

    override fun getPostConditions(): PostConditions<OrderStatusAdvance> =
        PostConditions(finalStatusIsNextCondition())

    private fun orderExistsCondition() = PreCondition<OrderStatusAdvance> { orderStatusAdvance ->
        if (orderRepository.orderIsMissing(orderStatusAdvance.orderId)) {
            throw OrderNotFoundException(ErrorMessage("Order ${orderStatusAdvance.orderId.value} not found."))
        }
    }

    // Another administrator may have changed the status in the meantime
    private fun initialStatusIsCurrentCondition() = PreCondition<OrderStatusAdvance> { orderStatusAdvance ->
        val order = orderRepository.findById(orderStatusAdvance.orderId)!!
        if (order.status != orderStatusAdvance.currentStatus) {
            val message = "Order ${order.number()} is ${order.status}, not ${orderStatusAdvance.currentStatus}."
            throw OrderStatusChangedException(ErrorMessage(message))
        }
    }

    private fun initialStatusHasNextCondition() = PreCondition<OrderStatusAdvance> { orderStatusAdvance ->
        if (orderStatusAdvance.currentStatus.isNotMovedByAnAdministrator()) {
            val message = "An order ${orderStatusAdvance.currentStatus} is not moved by an administrator."
            throw OrderStatusCannotAdvanceException(ErrorMessage(message))
        }
    }

    private fun finalStatusIsNextCondition() = PostCondition<OrderStatusAdvance> { orderStatusAdvance ->
        val expectedStatus = orderStatusAdvance.currentStatus.next()
        val storedStatus = orderRepository.findById(orderStatusAdvance.orderId)?.status
        if (storedStatus != expectedStatus) {
            val orderId = orderStatusAdvance.orderId.value
            val message = "Order $orderId is $storedStatus instead of $expectedStatus."
            throw OrderStatusAdvanceFailed(ErrorMessage(message))
        }
    }
}
