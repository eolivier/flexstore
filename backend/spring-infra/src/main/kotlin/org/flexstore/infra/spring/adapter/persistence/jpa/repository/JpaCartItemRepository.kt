package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.CartItemEntity
import org.flexstore.domain.cart.*
import org.flexstore.domain.sharedkernel.*
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaCartItemRepository : JpaRepository<CartItemEntity, String> {
    fun findAllByCartId(cartId: String, sort: Sort): List<CartItemEntity>
}
