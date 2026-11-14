package org.flexstore.domain.order.usecase

import org.flexstore.domain.actor.isNotAdmin
import org.flexstore.domain.order.ShippingMethodRepository
import org.flexstore.domain.order.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator adds a shipping method: it has a name and a cost that is not negative
class CreateShippingMethodUseCase(
    private val shippingMethodRepository: ShippingMethodRepository
) : UseCase<ShippingMethodCreation> {

    override fun getPreConditions(): PreConditions<ShippingMethodCreation> =
        PreConditions(creatorIsAdministratorCondition(), shippingMethodIsValidCondition())

    override fun getNominalScenario(): NominalScenario<ShippingMethodCreation> =
        NominalScenario(Step { creation -> shippingMethodRepository.save(creation.method) })

    override fun getPostConditions(): PostConditions<ShippingMethodCreation> =
        PostConditions(shippingMethodIsStoredCondition())

    // The creator, resolved before the use case runs, must be an Admin
    private fun creatorIsAdministratorCondition() = PreCondition<ShippingMethodCreation> { creation ->
        if (creation.creator.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can add a shipping method.")
            throw ShippingMethodAdministrationForbiddenException(message)
        }
    }

    private fun shippingMethodIsValidCondition() = PreCondition<ShippingMethodCreation> { creation ->
        if (creation.method.isInvalid()) {
            val message = ErrorMessage("A shipping method has a name and a cost that is not negative.")
            throw InvalidShippingMethodException(message)
        }
    }

    private fun shippingMethodIsStoredCondition() = PostCondition<ShippingMethodCreation> { creation ->
        if (shippingMethodRepository.shippingMethodIsMissing(creation.method.id)) {
            val message = ErrorMessage("The shipping method ${creation.method.id} was not stored.")
            throw ShippingMethodCreationFailed(message)
        }
    }
}
