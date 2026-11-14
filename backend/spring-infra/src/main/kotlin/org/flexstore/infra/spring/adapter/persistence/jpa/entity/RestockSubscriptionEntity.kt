package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// A customer who waits for a product out of stock, to be notified by email when it is back
@Entity
@Table(name = "restock_subscription")
data class RestockSubscriptionEntity(
    @Id
    val id: String,

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: UserEntity,

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    val product: ProductEntity
) : TimestampedEntity()
