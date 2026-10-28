package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// The orders of a customer, newest first
class ListCustomerOrdersUseCase(
    private val orderRepository: OrderRepository
) : QueryUseCase<UserId.ValidUserId, Orders> {

    override fun getPreConditions(): PreConditions<UserId.ValidUserId> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<UserId.ValidUserId, Orders> { customer ->
        orderRepository.findAllBy(customer).newestFirst()
    }

    override fun getPostConditions(): PostConditions<Orders> = PostConditions.none()
}
