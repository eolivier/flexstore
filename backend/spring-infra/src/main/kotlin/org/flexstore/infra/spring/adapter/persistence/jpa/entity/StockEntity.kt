package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The number of units that the shop has of a product, and the number at or under which the administrators are
// warned that it runs low
@Entity
@Table(name = "stock")
data class StockEntity(
    @Id
    val id: String,

    @Column(name = "product_id", nullable = false, unique = true, updatable = false)
    val productId: String,

    @Column(nullable = false)
    val quantity: Int,

    @Column(name = "alert_threshold", nullable = false)
    val alertThreshold: Int
) : TimestampedEntity()
