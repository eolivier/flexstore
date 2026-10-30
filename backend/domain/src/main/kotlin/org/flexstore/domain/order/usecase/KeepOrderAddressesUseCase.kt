package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// The addresses of an order are kept among the addresses of the customer (an address entered when ordering is
// added), so that the order refers to them. Only a known customer, whose account allows ordering, orders
class KeepOrderAddressesUseCase(private val userRepository: UserRepository) : UseCase<OrderPlacement> {

    override fun getPreConditions(): PreConditions<OrderPlacement> =
        PreConditions(customerIsKnownCondition(), accountAllowsOrderingCondition())

    override fun getNominalScenario(): NominalScenario<OrderPlacement> {
        val keepAddressesStep = Step<OrderPlacement> { orderPlacement ->
            val (delivery, billing) = orderPlacement.addresses
            val definedUser = definedUserOf(orderPlacement)
            userRepository.save(definedUser.withPostalAddressesIncluding(delivery, billing))
        }
        return NominalScenario(keepAddressesStep)
    }

    override fun getPostConditions(): PostConditions<OrderPlacement> = PostConditions.none()

    // Only called once the customer is known
    private fun definedUserOf(orderPlacement: OrderPlacement) =
        userRepository.findById(orderPlacement.customer.userId) as User.DefinedUser

    private fun customerIsKnownCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        val userId = orderPlacement.customer.userId
        if (userRepository.notExists(userId)) {
            val message = ErrorMessage("The customer ${userId.value} is unknown: they cannot order.")
            throw UserNotFoundException(message)
        }
    }

    // A token stays valid after the account is suspended, locked or disabled: the status is checked at each
    // order
    private fun accountAllowsOrderingCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        val definedUser = definedUserOf(orderPlacement)
        if (definedUser.status.refusesOrdering()) {
            val accountId = definedUser.id.value
            val message = ErrorMessage("The account $accountId is ${definedUser.status}: it cannot order.")
            throw OrderRefusedException(definedUser.status, message)
        }
    }
}
