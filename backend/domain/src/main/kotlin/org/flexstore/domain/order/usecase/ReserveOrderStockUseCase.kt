package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.stock.*
import org.ucop.domain.entity.*

// The units of a placed order are reserved until it is paid, or cancelled for lack of payment. Another order may
// have taken them since the stock was checked: the order is then refused (the caller keeps nothing of it)
class ReserveOrderStockUseCase(
    private val orderRepository: OrderRepository,
    private val stockRepository: StockRepository
) : UseCase<OrderId> {

    override fun getPreConditions(): PreConditions<OrderId> = PreConditions(orderExistsCondition())

    override fun getNominalScenario(): NominalScenario<OrderId> {
        val reserveStockStep = Step<OrderId> { orderId ->
            val order = orderRepository.findById(orderId)!!
            val reservations = order.lines.asList().associateWith { line ->
                stockRepository.reserve(line.productId, line.quantity)
            }
            val refusedLines = reservations.filterValues { it.isNotCovered() }.keys
            if (refusedLines.isNotEmpty()) {
                val productNames = refusedLines.joinToString { it.productName.value }
                throw OutOfStockException(ErrorMessage("Not enough units in stock for: $productNames."))
            }
        }
        return NominalScenario(reserveStockStep)
    }

    override fun getPostConditions(): PostConditions<OrderId> = PostConditions.none()

    private fun orderExistsCondition() = PreCondition<OrderId> { orderId ->
        if (orderRepository.orderIsMissing(orderId)) {
            throw OrderNotFoundException(ErrorMessage("Order ${orderId.value} not found."))
        }
    }
}
