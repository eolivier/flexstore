package org.flexstore.domain.sales.usecase

import org.flexstore.domain.order.OrderRepository
import org.flexstore.domain.sales.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// The sales of the shop over the last 7, 30 or 90 days: revenue, orders per day and best sellers
class ReadSalesDashboardUseCase(
    private val orderRepository: OrderRepository
) : QueryUseCase<SalesQuestion, SalesDashboard> {

    override fun getPreConditions(): PreConditions<SalesQuestion> = PreConditions(periodIsAllowedCondition())

    override fun getNominalQuery() = NominalQuery<SalesQuestion, SalesDashboard> { question ->
        SalesDashboard.of(orderRepository.findAll(), question.period())
    }

    override fun getPostConditions(): PostConditions<SalesDashboard> = PostConditions.none()

    private fun periodIsAllowedCondition() = PreCondition<SalesQuestion> { question ->
        if (question.dayCount.isNotAllowed()) {
            throw InvalidSalesPeriodException(ErrorMessage("The sales are given over 7, 30 or 90 days."))
        }
    }
}
