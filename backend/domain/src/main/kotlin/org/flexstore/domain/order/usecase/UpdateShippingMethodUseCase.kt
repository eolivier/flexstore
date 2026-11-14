package org.flexstore.domain.order.usecase

import org.flexstore.domain.actor.isNotAdmin
import org.flexstore.domain.order.ShippingMethodRepository
import org.flexstore.domain.order.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator changes a shipping method (its name, its cost, whether it is free from the threshold, whether it
// is enabled): at least one method stays enabled, so that the customers can always choose one. The orders already
// placed keep their shipping cost
class UpdateShippingMethodUseCase(
    private val shippingMethodRepository: ShippingMethodRepository
) : UseCase<ShippingMethodUpdate> {

    override fun getPreConditions(): PreConditions<ShippingMethodUpdate> = PreConditions(
        editorIsAdministratorCondition(), shippingMethodExistsCondition(), shippingMethodIsValidCondition(),
        oneShippingMethodStaysEnabledCondition()
    )

    override fun getNominalScenario(): NominalScenario<ShippingMethodUpdate> =
        NominalScenario(Step { update -> shippingMethodRepository.save(update.method) })

    override fun getPostConditions(): PostConditions<ShippingMethodUpdate> = PostConditions.none()

    // The editor, resolved before the use case runs, must be an Admin
    private fun editorIsAdministratorCondition() = PreCondition<ShippingMethodUpdate> { update ->
        if (update.editor.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can change a shipping method.")
            throw ShippingMethodAdministrationForbiddenException(message)
        }
    }

    private fun shippingMethodExistsCondition() = PreCondition<ShippingMethodUpdate> { update ->
        if (shippingMethodRepository.shippingMethodIsMissing(update.method.id)) {
            throw ShippingMethodNotFoundException("No shipping method ${update.method.id}.")
        }
    }

    private fun shippingMethodIsValidCondition() = PreCondition<ShippingMethodUpdate> { update ->
        if (update.method.isInvalid()) {
            val message = ErrorMessage("A shipping method has a name and a cost that is not negative.")
            throw InvalidShippingMethodException(message)
        }
    }

    private fun oneShippingMethodStaysEnabledCondition() = PreCondition<ShippingMethodUpdate> { update ->
        val changedShippingMethods = shippingMethodRepository.findAll().replacedBy(update.method)
        if (changedShippingMethods.hasNoEnabledMethod()) {
            val message = ErrorMessage("At least one shipping method must stay enabled.")
            throw NoShippingMethodLeftException(message)
        }
    }
}
