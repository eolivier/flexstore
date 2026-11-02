package org.flexstore.domain.sales.usecase

import org.flexstore.domain.order.OrderRepository
import org.flexstore.domain.sales.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import org.flexstore.domain.user.UserRepository

// An administrator reads the sales of the shop over the last 7, 30 or 90 days: revenue, orders per day and best
// sellers
class ReadSalesDashboardUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<SalesDashboardReading, SalesDashboard> {

    override fun getPreConditions(): PreConditions<SalesDashboardReading> =
        PreConditions(readerIsAdministratorCondition(), periodIsAllowedCondition())

    override fun getNominalQuery() = NominalQuery<SalesDashboardReading, SalesDashboard> { (_, question) ->
        SalesDashboard.of(orderRepository.findAll(), question.period())
    }

    override fun getPostConditions(): PostConditions<SalesDashboard> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<SalesDashboardReading> { request ->
        if (userRepository.isNotAdministrator(request.reader)) {
            throw SalesDashboardForbiddenException(ErrorMessage("Only an administrator can read the sales of the shop."))
        }
    }

    private fun periodIsAllowedCondition() = PreCondition<SalesDashboardReading> { (_, question) ->
        if (question.dayCount.isNotAllowed()) {
            throw InvalidSalesPeriodException(ErrorMessage("The sales are given over 7, 30 or 90 days."))
        }
    }
}
