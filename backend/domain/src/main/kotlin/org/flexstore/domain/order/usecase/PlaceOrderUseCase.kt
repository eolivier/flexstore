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
    // added), so that the order refers to them; then the order is placed and the cart emptied
    override fun getNominalScenario(): NominalScenario<OrderPlacement> {
        val keepAddressesStep = Step<OrderPlacement> { orderPlacement ->
            val customer = userRepository.findById(orderPlacement.customer.userId) as User.DefinedUser
            val orderAddresses = orderPlacement.addresses
            val customerWithAddresses = customer.withPostalAddressesIncluding(orderAddresses.delivery, orderAddresses.billing)
            userRepository.save(customerWithAddresses)
        }
        val placeOrderStep = Step<OrderPlacement> { orderPlacement ->
            val cart = cartRepository.load(orderPlacement.customer)
            val order = Order.place(orderPlacement, cart)
            orderRepository.save(order)
            cartRepository.save(cart.emptied())
        }
        return NominalScenario(listOf(keepAddressesStep, placeOrderStep))
    }

    override fun getPostConditions(): PostConditions<OrderPlacement> =
        PostConditions(orderIsStoredCondition(), cartIsEmptyCondition())

    private fun customerIsKnownCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        val customerId = orderPlacement.customer.userId
        if (userRepository.notExists(customerId)) {
            throw UserNotFoundException(ErrorMessage("The customer ${customerId.value} is unknown: they cannot order."))
        }
    }

    // A token stays valid after the account is suspended, locked or disabled: the status is checked at each order
    private fun accountAllowsOrderingCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        val customer = userRepository.findById(orderPlacement.customer.userId) as User.DefinedUser
        if (customer.status.refusesOrdering()) {
            throw OrderRefusedException(customer.status, ErrorMessage("The account ${customer.id.value} is ${customer.status}: it cannot order."))
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

    private fun cartIsEmptyCondition() = PostCondition<OrderPlacement> { orderPlacement ->
        if (cartRepository.load(orderPlacement.customer).hasItems()) {
            throw OrderPlacementFailed(ErrorMessage("The cart was not emptied after order ${orderPlacement.orderId.value}."))
        }
    }
}
