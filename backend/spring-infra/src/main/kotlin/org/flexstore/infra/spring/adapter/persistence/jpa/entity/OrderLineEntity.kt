package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.math.BigDecimal

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

    // The rate of the product when the order was placed
    @Column(name = "vat_rate", nullable = false)
    val vatRate: String,

    @Column(nullable = false)
    val quantity: Int
) : TimestampedEntity()
