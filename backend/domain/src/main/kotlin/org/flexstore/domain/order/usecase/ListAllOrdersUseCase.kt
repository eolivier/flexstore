package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.ucop.domain.entity.*

// The orders of all the customers, newest first
class ListAllOrdersUseCase(private val orderRepository: OrderRepository) : QueryUseCase<Unit, Orders> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, Orders> { orderRepository.findAll().newestFirst() }

    override fun getPostConditions(): PostConditions<Orders> = PostConditions.none()
}
