package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*
import java.time.Instant

class PlaceOrderUseCase(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : UseCase<OrderPlacement> {

    override fun getPreConditions(): PreConditions<OrderPlacement> = PreConditions(accountAllowsOrderingCondition(), cartHasItemsCondition(), addressesAreCompleteCondition())

    override fun getNominalScenario(): NominalScenario<OrderPlacement> {
        val placeOrderStep = Step<OrderPlacement> { request ->
            val cart = cartRepository.load(request.customer)
            orderRepository.save(Order.place(request, cart))
            cartRepository.save(cart.emptied())
        }
        return NominalScenario(listOf(placeOrderStep))
    }

    override fun getPostConditions(): PostConditions<OrderPlacement> =
        PostConditions(orderIsStoredCondition(), cartIsEmptyCondition())

    // A token stays valid after the account is suspended, locked or disabled: the status is checked at each order
    private fun accountAllowsOrderingCondition() = PreCondition<OrderPlacement> { request ->
        val customerId = request.customer.userId
        val customer = if (userRepository.exists(customerId)) userRepository.findById(customerId) else null
        if (customer is User.DefinedUser && customer.status.refusesOrdering()) {
            throw OrderRefusedException(customer.status, ErrorMessage("The account ${request.customer.userId.value} is ${customer.status}: it cannot order."))
        }
    }

    private fun cartHasItemsCondition() = PreCondition<OrderPlacement> { request ->
        if (cartRepository.load(request.customer).isEmpty()) {
            throw EmptyCartException(ErrorMessage("An order cannot be placed from an empty cart."))
        }
    }

    private fun addressesAreCompleteCondition() = PreCondition<OrderPlacement> { request ->
        if (request.addresses.isIncomplete()) {
            throw IncompleteOrderAddressException(ErrorMessage("An order needs a complete delivery address and billing address."))
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
