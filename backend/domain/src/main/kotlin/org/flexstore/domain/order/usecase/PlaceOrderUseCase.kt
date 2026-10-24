package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*
import java.time.Instant

class PlaceOrderUseCase(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : UseCase<OrderPlacement> {

    override fun getPreConditions(): List<PreCondition<OrderPlacement>> = listOf(cartHasItemsCondition())

    override fun getNominalScenario(): NominalScenario<OrderPlacement> {
        val placeOrderStep = Step<OrderPlacement> { request ->
            val cart = cartRepository.load(request.customer)
            orderRepository.save(Order.place(request, cart))
            cartRepository.save(cart.emptied())
        }
        return NominalScenario(listOf(placeOrderStep))
    }

    override fun getPostConditions(): List<PostCondition<OrderPlacement>> =
        listOf(orderIsStoredCondition(), cartIsEmptyCondition())

    private fun cartHasItemsCondition() = PreCondition<OrderPlacement> { request ->
        if (cartRepository.load(request.customer).isEmpty()) {
            throw EmptyCartException(ErrorMessage("An order cannot be placed from an empty cart."))
        }
    }

    private fun orderIsStoredCondition() = PostCondition<OrderPlacement> { request ->
        if (orderRepository.orderIsMissing(request.orderId)) {
            throw OrderPlacementFailed(ErrorMessage("Order ${request.orderId.value} was not stored."))
        }
    }

    private fun cartIsEmptyCondition() = PostCondition<OrderPlacement> { request ->
        if (cartRepository.load(request.customer).hasItems()) {
            throw OrderPlacementFailed(ErrorMessage("The cart was not emptied after order ${request.orderId.value}."))
        }
    }
}
