package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The number of units that the shop has of a product
@Entity
@Table(name = "stock")
data class StockEntity(
    @Id
    val id: String,

    @Column(name = "product_id", nullable = false, unique = true, updatable = false)
    val productId: String,

    @Column(nullable = false)
    val quantity: Int
) : TimestampedEntity()
