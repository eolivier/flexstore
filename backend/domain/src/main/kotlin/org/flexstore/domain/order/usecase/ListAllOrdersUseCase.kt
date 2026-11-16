package org.flexstore.domain.order.usecase

import org.flexstore.domain.actor.isNotAdmin
import org.flexstore.domain.actor.isNotSystem
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// The orders of all the customers, newest first, for an administrator or for the system, which runs the
// scheduled use cases
class ListAllOrdersUseCase(private val orderRepository: OrderRepository) : QueryUseCase<Actor, Orders> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(readerIsAdministratorOrSystemCondition())

    override fun getNominalQuery() = NominalQuery<Actor, Orders> { orderRepository.findAll().newestFirst() }

    override fun getPostConditions(): PostConditions<Orders> = PostConditions.none()

    private fun readerIsAdministratorOrSystemCondition() = PreCondition<Actor> { reader ->
        if (reader.isNotAdmin() && reader.isNotSystem()) {
            val message = ErrorMessage("Only an administrator or the system can read the orders of all the customers.")
            throw OrderAdministrationForbiddenException(message)
        }
    }
}
