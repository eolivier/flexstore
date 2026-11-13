package org.flexstore.infra.spring.adapter.persistence.jpa.repository.order

import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.OrderEntity
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaOrderRepository : JpaRepository<OrderEntity, String> {
    fun findAllByUserId(userId: String): List<OrderEntity>

    fun findAllByUserId(userId: String, pageable: Pageable): org.springframework.data.domain.Page<OrderEntity>
}
