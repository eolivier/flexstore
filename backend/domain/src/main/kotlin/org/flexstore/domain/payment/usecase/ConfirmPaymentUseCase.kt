package org.flexstore.domain.payment.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.stock.StockRepository
import org.ucop.domain.entity.*

// The payment provider confirms the payment of an order: the order is paid, its units are taken out of the
// stock, and the cart of its customer emptied
class ConfirmPaymentUseCase(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val stockRepository: StockRepository
) : UseCase<OrderId> {

    override fun getPreConditions(): PreConditions<OrderId> =
        PreConditions(orderExistsCondition(), orderAwaitsPaymentCondition())

    override fun getNominalScenario(): NominalScenario<OrderId> {
        val payStep = Step<OrderId> { orderId ->
            val order = orderRepository.findById(orderId)!!
            orderRepository.save(order.advanced())
        }
        val emptyCartStep = Step<OrderId> { orderId ->
            val customer = CartOwner.Customer(orderRepository.findById(orderId)!!.customer)
            cartRepository.save(cartRepository.load(customer).emptied())
        }
        val withdrawStockStep = Step<OrderId> { orderId ->
            val order = orderRepository.findById(orderId)!!
            order.lines.asList().forEach { line ->
                val stock = stockRepository.stockOf(line.productId)
                stockRepository.save(stock.withdrawn(line.quantity))
            }
        }
        return NominalScenario(listOf(payStep, withdrawStockStep, emptyCartStep))
    }

    override fun getPostConditions(): PostConditions<OrderId> = PostConditions(orderIsPaidCondition())

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
