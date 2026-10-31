package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

// A discount code: PERCENTAGE (1 to 100) or AMOUNT (in euros) off the items of an order
@Entity
@Table(name = "discount_code")
data class DiscountCodeEntity(
    @Id
    val id: String,

    @Column(nullable = false, unique = true, updatable = false)
    val code: String,

    @Column(name = "reduction_type", nullable = false)
    val reductionType: String,

    @Column(name = "reduction_value", nullable = false)
    val reductionValue: BigDecimal,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    @Column(name = "usage_limit", nullable = false)
    val usageLimit: Int,

    @Column(name = "times_used", nullable = false)
    val timesUsed: Int
) : TimestampedEntity()
