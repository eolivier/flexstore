package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.ShippingMethodRepository
import org.flexstore.domain.order.model.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer reads the shipping method they choose to check out: it exists and is still enabled
class ReadEnabledShippingMethodUseCase(
    private val shippingMethodRepository: ShippingMethodRepository,
    private val userRepository: UserRepository
) : QueryUseCase<EnabledShippingMethodLookup, ShippingMethod> {

    override fun getPreConditions(): PreConditions<EnabledShippingMethodLookup> = PreConditions(
        customerCanActCondition(), shippingMethodExistsCondition(), shippingMethodIsEnabledCondition()
    )

    override fun getNominalQuery() = NominalQuery<EnabledShippingMethodLookup, ShippingMethod> { lookup ->
        shippingMethodRepository.get(lookup.shippingMethodId)
    }

    override fun getPostConditions(): PostConditions<ShippingMethod> = PostConditions.none()

    // The customer must be an existing user whose account is not suspended, locked or disabled (read
    // from the users, not from the request)
    private fun customerCanActCondition() = PreCondition<EnabledShippingMethodLookup> { lookup ->
        userRepository.verifyCustomerCanAct(lookup.customer.userId)
    }

    private fun shippingMethodExistsCondition() = PreCondition<EnabledShippingMethodLookup> { lookup ->
        if (shippingMethodRepository.shippingMethodIsMissing(lookup.shippingMethodId)) {
            throw ShippingMethodNotFoundException("No shipping method ${lookup.shippingMethodId}.")
        }
    }

    private fun shippingMethodIsEnabledCondition() = PreCondition<EnabledShippingMethodLookup> { lookup ->
        if (shippingMethodRepository.get(lookup.shippingMethodId).isDisabled()) {
            val message = ErrorMessage("The shipping method ${lookup.shippingMethodId} is no longer proposed.")
            throw ShippingMethodDisabledException(message)
        }
    }
}
