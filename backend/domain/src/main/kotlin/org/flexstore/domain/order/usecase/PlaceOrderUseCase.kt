package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

class PlaceOrderUseCase(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : UseCase<OrderPlacement> {

    override fun getPreConditions(): PreConditions<OrderPlacement> =
        PreConditions(customerIsKnownCondition(), accountAllowsOrderingCondition(), cartHasItemsCondition(), addressesAreCompleteCondition())

    // The addresses of the order are kept among the addresses of the customer (an address entered when ordering is
    // added), so that the order refers to them; then the order is placed. The cart is emptied once it is paid
    override fun getNominalScenario(): NominalScenario<OrderPlacement> {
        val keepAddressesStep = Step<OrderPlacement> { orderPlacement ->
            val definedUser = userRepository.findById(orderPlacement.customer.userId) as User.DefinedUser
            val orderAddresses = orderPlacement.addresses
            val definedUserWithAddresses = definedUser.withPostalAddressesIncluding(orderAddresses.delivery, orderAddresses.billing)
            userRepository.save(definedUserWithAddresses)
        }
        val placeOrderStep = Step<OrderPlacement> { orderPlacement ->
            val cart = cartRepository.load(orderPlacement.customer)
            val order = Order.place(orderPlacement, cart)
            orderRepository.save(order)
        }
        return NominalScenario(listOf(keepAddressesStep, placeOrderStep))
    }

    override fun getPostConditions(): PostConditions<OrderPlacement> =
        PostConditions(orderIsStoredCondition())

    private fun customerIsKnownCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        val userId = orderPlacement.customer.userId
        if (userRepository.notExists(userId)) {
            throw UserNotFoundException(ErrorMessage("The customer ${userId.value} is unknown: they cannot order."))
        }
    }

    // A token stays valid after the account is suspended, locked or disabled: the status is checked at each order
    private fun accountAllowsOrderingCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        val definedUser = userRepository.findById(orderPlacement.customer.userId) as User.DefinedUser
        if (definedUser.status.refusesOrdering()) {
            throw OrderRefusedException(definedUser.status, ErrorMessage("The account ${definedUser.id.value} is ${definedUser.status}: it cannot order."))
        }
    }

    private fun cartHasItemsCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        if (cartRepository.load(orderPlacement.customer).isEmpty()) {
            throw EmptyCartException(ErrorMessage("An order cannot be placed from an empty cart."))
        }
    }

    private fun addressesAreCompleteCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        if (orderPlacement.addresses.isIncomplete()) {
            throw IncompleteOrderAddressException(ErrorMessage("An order needs a complete delivery address and billing address."))
        }
    }

    private fun orderIsStoredCondition() = PostCondition<OrderPlacement> { orderPlacement ->
        if (orderRepository.orderIsMissing(orderPlacement.orderId)) {
            throw OrderPlacementFailed(ErrorMessage("Order ${orderPlacement.orderId.value} was not stored."))
        }
    }
}
