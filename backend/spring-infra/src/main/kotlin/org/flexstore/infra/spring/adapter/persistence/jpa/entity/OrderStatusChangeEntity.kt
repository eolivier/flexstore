package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// A status an order reached, at its position in the history of the order (0: placed)
@Entity
@Table(name = "customer_order_status_change")
data class OrderStatusChangeEntity(
    @Id
    val id: String,

    @Column(nullable = false, updatable = false)
    val position: Int,

    @Column(nullable = false, updatable = false)
    val status: String,

    @Column(name = "changed_at", nullable = false, updatable = false)
    val changedAt: Instant
) : TimestampedEntity()
