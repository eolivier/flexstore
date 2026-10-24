package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cart_item")
data class CartItemEntity(
    @Id
    val id: String,

    @Column(name = "cart_id", nullable = false, updatable = false)
    val cartId: String,

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    val product: ProductEntity,

    @Column(nullable = false)
    val quantity: Int,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
