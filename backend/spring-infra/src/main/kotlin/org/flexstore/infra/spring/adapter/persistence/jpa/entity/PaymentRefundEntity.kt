package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The refund of a payment on the payment provider, at most one per payment
@Entity
@Table(name = "payment_refund")
data class PaymentRefundEntity(
    @Id
    val id: String,

    @Column(name = "payment_id", nullable = false, unique = true, updatable = false)
    val paymentId: String,

    @Column(name = "provider_reference", nullable = false, unique = true, updatable = false)
    val providerReference: String
) : TimestampedEntity()
