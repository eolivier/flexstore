package org.flexstore.infra.spring.adapter.persistence.jpa.repository.restock

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.RestockSubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaRestockSubscriptionRepository : JpaRepository<RestockSubscriptionEntity, String> {
    fun findAllByUser_Id(userId: String): List<RestockSubscriptionEntity>

    fun existsByUser_IdAndProduct_Id(userId: String, productId: String): Boolean

    fun deleteByUser_IdAndProduct_Id(userId: String, productId: String)
}
