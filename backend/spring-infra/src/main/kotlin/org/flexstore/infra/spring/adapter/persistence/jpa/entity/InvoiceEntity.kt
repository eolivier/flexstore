package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// The invoice of a paid order, numbered from the sequence invoice_number_seq
@Entity
@Table(name = "invoice")
data class InvoiceEntity(
    @Id
    val id: String,

    @Column(nullable = false, unique = true, updatable = false)
    val number: String,

    @Column(name = "customer_order_id", nullable = false, unique = true, updatable = false)
    val customerOrderId: String,

    @Column(name = "issued_at", nullable = false, updatable = false)
    val issuedAt: Instant
) : TimestampedEntity()
