package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// A payment started on the payment provider for an order (the order refers to it), with its refund if any
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
    val outcome: String,

    // The reference of the refund of the payment on the payment provider, null while it is not refunded
    @Column(name = "refund_provider_reference", unique = true)
    val refundProviderReference: String? = null
) : TimestampedEntity()
