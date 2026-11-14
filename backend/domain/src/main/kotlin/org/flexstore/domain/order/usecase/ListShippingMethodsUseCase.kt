package org.flexstore.domain.order.usecase

import org.flexstore.domain.actor.isNotAdmin
import org.flexstore.domain.order.ShippingMethodRepository
import org.flexstore.domain.order.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator reads all the shipping methods, the disabled ones included, the cheapest first
class ListShippingMethodsUseCase(
    private val shippingMethodRepository: ShippingMethodRepository
) : QueryUseCase<ShippingMethodsReading, ShippingMethods> {

    override fun getPreConditions(): PreConditions<ShippingMethodsReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() =
        NominalQuery<ShippingMethodsReading, ShippingMethods> { shippingMethodRepository.findAll() }

    override fun getPostConditions(): PostConditions<ShippingMethods> = PostConditions.none()

    // The reader, resolved before the use case runs, must be an Admin
    private fun readerIsAdministratorCondition() = PreCondition<ShippingMethodsReading> { reading ->
        if (reading.reader.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can read the shipping methods.")
            throw ShippingMethodAdministrationForbiddenException(message)
        }
    }
}
