package org.flexstore.domain.payment.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.stock.StockRepository
import org.ucop.domain.entity.*

// The payment provider confirms the payment of an order: the order is paid (its units were reserved when it
// was placed), and the cart of its customer emptied. An order cancelled while its customer was paying is paid
// all the same: its units are taken out of the stock again
class ConfirmPaymentUseCase(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val stockRepository: StockRepository
) : UseCase<OrderId> {

    override fun getPreConditions(): PreConditions<OrderId> =
        PreConditions(orderExistsCondition(), orderAwaitsPaymentCondition())

    override fun getNominalScenario(): NominalScenario<OrderId> {
        val withdrawStockStep = Step<OrderId> { orderId ->
            val order = orderRepository.findById(orderId)!!
            if (order.isCancelled()) withdrawStockOf(order)
        }
        val payStep = Step<OrderId> { orderId ->
            val order = orderRepository.findById(orderId)!!
            orderRepository.save(order.paidAt(StatusChangedAt.now()))
        }
        val emptyCartStep = Step<OrderId> { orderId ->
            val customer = CartOwner.Customer(orderRepository.findById(orderId)!!.customer)
            cartRepository.save(cartRepository.load(customer).emptied())
        }
        return NominalScenario(listOf(withdrawStockStep, payStep, emptyCartStep))
    }

    override fun getPostConditions(): PostConditions<OrderId> = PostConditions(orderIsPaidCondition())

    // What is left of the units: the order is paid anyway, and the stock never goes below zero
    private fun withdrawStockOf(order: Order) = order.lines.asList().forEach { line ->
        val availableQuantity = stockRepository.stockOf(line.productId).availableOf(line.quantity)
        stockRepository.reserve(line.productId, availableQuantity)
    }

    private fun orderExistsCondition() = PreCondition<OrderId> { orderId ->
        if (orderRepository.orderIsMissing(orderId)) {
            throw OrderNotFoundException(ErrorMessage("Order ${orderId.value} not found."))
        }
    }

    private fun orderAwaitsPaymentCondition() = PreCondition<OrderId> { orderId ->
        val order = orderRepository.findById(orderId)!!
        if (order.isPaid()) {
            throw OrderAlreadyPaidException(ErrorMessage("Order ${order.number()} is already paid."))
        }
    }

    private fun orderIsPaidCondition() = PostCondition<OrderId> { orderId ->
        if (orderRepository.findById(orderId)?.status != OrderStatus.PAID) {
            throw PaymentConfirmationFailed(ErrorMessage("Order ${orderId.value} is not paid."))
        }
    }
}
