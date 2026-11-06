package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator reads the orders of all the customers, newest first
class ListAllOrdersUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<AllOrdersReading, Orders> {

    override fun getPreConditions(): PreConditions<AllOrdersReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() =
        NominalQuery<AllOrdersReading, Orders> { orderRepository.findAll().newestFirst() }

    override fun getPostConditions(): PostConditions<Orders> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<AllOrdersReading> { reading ->
        if (userRepository.isNotAdministrator(reading.reader)) {
            throw OrderAdministrationForbiddenException(
                ErrorMessage("Only an administrator can read the orders of all the customers.")
            )
        }
    }
}
