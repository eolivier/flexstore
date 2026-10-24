package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "customer_order")
data class OrderEntity(
    @Id
    val id: String,

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: String,

    @Column(nullable = false)
    val status: String,

    @Column(name = "placed_at", nullable = false, updatable = false)
    val placedAt: Instant,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @OrderBy("position")
    val lines: List<OrderLineEntity> = emptyList()
) : TimestampedEntity()
