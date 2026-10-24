package org.flexstore.domain.order

import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.CartRepository
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*
import java.time.Instant

// The id of the new order is chosen by the caller, which can then read the placed order
data class PlaceOrderRequest(val orderId: OrderId, val customer: CartOwner.Customer, val placedAt: Instant)

class PlaceOrderUseCase(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : UseCase<PlaceOrderRequest> {

    override fun getPreConditions(): List<PreCondition<PlaceOrderRequest>> = listOf(cartHasItemsCondition())

    override fun getNominalScenario(): NominalScenario<PlaceOrderRequest> {
        val placeOrderStep = Step<PlaceOrderRequest> { request ->
            val cart = cartRepository.load(request.customer)
            orderRepository.save(Order.place(request, cart))
            cartRepository.save(cart.emptied())
        }
        return NominalScenario(listOf(placeOrderStep))
    }

    override fun getPostConditions(): List<PostCondition<PlaceOrderRequest>> =
        listOf(orderIsStoredCondition(), cartIsEmptyCondition())

    private fun cartHasItemsCondition() = PreCondition<PlaceOrderRequest> { request ->
        if (cartRepository.load(request.customer).isEmpty()) {
            throw EmptyCartException(NonEmptyString("An order cannot be placed from an empty cart."))
        }
    }

    private fun orderIsStoredCondition() = PostCondition<PlaceOrderRequest> { request ->
        if (orderRepository.orderIsMissing(request.orderId)) {
            throw OrderPlacementFailed(NonEmptyString("Order ${request.orderId.value} was not stored."))
        }
    }

    private fun cartIsEmptyCondition() = PostCondition<PlaceOrderRequest> { request ->
        if (cartRepository.load(request.customer).hasItems()) {
            throw OrderPlacementFailed(NonEmptyString("The cart was not emptied after order ${request.orderId.value}."))
        }
    }
}
