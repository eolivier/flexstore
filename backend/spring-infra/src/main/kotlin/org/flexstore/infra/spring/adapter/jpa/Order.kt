package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*
import java.math.BigDecimal
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
)

@Entity
@Table(name = "customer_order_line")
data class OrderLineEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    val position: Int,

    @Column(name = "product_id", nullable = false)
    val productId: String,

    @Column(name = "product_name", nullable = false)
    val productName: String,

    @Column(name = "unit_price", nullable = false)
    val unitPrice: BigDecimal,

    @Column(nullable = false)
    val currency: String,

    @Column(nullable = false)
    val quantity: Int
)
