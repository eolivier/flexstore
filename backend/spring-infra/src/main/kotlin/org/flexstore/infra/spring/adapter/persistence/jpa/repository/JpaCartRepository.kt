package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.CartEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.CartItemEntity
import org.flexstore.domain.cart.*
import org.flexstore.domain.sharedkernel.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import org.springframework.stereotype.Repository

@Repository
interface JpaCartRepository : JpaRepository<CartEntity, String> {
    fun findByUserId(userId: String): CartEntity?

    @Query(
        "select c from CartEntity c where c.userId is not null and c.remindedAt is null " +
            "and c.lastChangedAt <= :limit " +
            "and exists (select i.id from CartItemEntity i where i.cartId = c.id) " +
            "order by c.lastChangedAt"
    )
    fun findAbandonedBefore(@Param("limit") limit: Instant): List<CartEntity>

    // The cart changed: it is not reminded any more, until it is left again
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update CartEntity c set c.lastChangedAt = :changedAt, c.remindedAt = null where c.id = :id")
    fun markChanged(@Param("id") id: String, @Param("changedAt") changedAt: Instant)

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update CartEntity c set c.remindedAt = :remindedAt where c.userId = :userId")
    fun markReminded(@Param("userId") userId: String, @Param("remindedAt") remindedAt: Instant)
}
