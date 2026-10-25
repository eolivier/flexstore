package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*

// A payment started on the payment provider for an order (the order refers to it)
@Entity
@Table(name = "payment")
data class PaymentEntity(
    @Id
    val id: String,

    @Column(name = "provider_reference", nullable = false, unique = true)
    val providerReference: String,

    @Column(name = "checkout_url", nullable = false)
    val checkoutUrl: String,

    // PENDING or PAID
    @Column(nullable = false)
    val outcome: String
) : TimestampedEntity()
