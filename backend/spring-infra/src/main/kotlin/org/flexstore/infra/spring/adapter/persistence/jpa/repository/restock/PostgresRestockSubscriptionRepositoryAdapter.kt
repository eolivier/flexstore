package org.flexstore.infra.spring.adapter.persistence.jpa.repository.restock

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.restock.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.RestockSubscriptionEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.product.JpaProductRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.user.JpaUserRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId

// The subscriptions to the return of a product, kept in PostgreSQL: the moment of a subscription is the moment
// its row was created
@Repository
@Transactional
class PostgresRestockSubscriptionRepositoryAdapter(
    private val jpaRepo: JpaRestockSubscriptionRepository,
    private val userRepo: JpaUserRepository,
    private val productRepo: JpaProductRepository
) : RestockSubscriptionRepository {

    override fun save(restockSubscription: RestockSubscription) {
        jpaRepo.save(restockSubscription.toEntity())
    }

    override fun remove(customer: UserId.ValidUserId, productId: ProductId) {
        jpaRepo.deleteByUser_IdAndProduct_Id(customer.value, productId.id.value)
    }

    @Transactional(readOnly = true)
    override fun contains(customer: UserId.ValidUserId, productId: ProductId) =
        jpaRepo.existsByUser_IdAndProduct_Id(customer.value, productId.id.value)

    @Transactional(readOnly = true)
    override fun findAllBy(customer: UserId.ValidUserId) =
        RestockSubscriptions(jpaRepo.findAllByUser_Id(customer.value).map { it.toDomain() })

    @Transactional(readOnly = true)
    override fun findAll() = RestockSubscriptions(jpaRepo.findAll().map { it.toDomain() })

    private fun RestockSubscription.toEntity() = RestockSubscriptionEntity(
        id = restockSubscriptionId.id.value,
        user = userRepo.getReferenceById(customer.value),
        product = productRepo.getReferenceById(productId.id.value)
    )

    // The creation date is set by Hibernate when the row is inserted, in the time zone of the application
    private fun RestockSubscriptionEntity.toDomain(): RestockSubscription {
        val subscribedAt = createdAt?.atZone(ZoneId.systemDefault())?.toInstant() ?: Instant.EPOCH
        return RestockSubscription(
            RestockSubscriptionId(Identity(id)),
            UserId.ValidUserId(user.id),
            ProductId(Identity(product.id)),
            SubscribedAt(subscribedAt)
        )
    }
}
