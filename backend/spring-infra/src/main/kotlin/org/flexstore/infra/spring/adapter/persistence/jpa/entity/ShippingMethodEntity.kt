package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.math.BigDecimal

// A shipping method: its name, its cost in euros, ELIGIBLE or NOT_ELIGIBLE to the free shipping from the
// threshold of the shop settings, and ENABLED or DISABLED
@Entity
@Table(name = "shipping_method")
data class ShippingMethodEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    val label: String,

    @Column(nullable = false)
    val cost: BigDecimal,

    @Column(name = "free_shipping_eligibility", nullable = false)
    val freeShippingEligibility: String,

    @Column(nullable = false)
    val status: String
) : TimestampedEntity()
