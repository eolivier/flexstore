package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// The invoice of a paid order, numbered in the sequence of its year (document_counter), and its credit note
// if any
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
    val issuedAt: Instant,

    // The version of the seller current when the invoice was issued
    @Column(name = "seller_id", nullable = false, updatable = false)
    val sellerId: String,

    // The credit note that cancels the invoice when its order is refunded: its number and when it was issued,
    // null together while there is none. Written by the credit note adapter only (updatable = false): saving the
    // invoice never overwrites them
    @Column(name = "credit_note_number", unique = true, updatable = false)
    val creditNoteNumber: String? = null,

    @Column(name = "credit_note_issued_at", updatable = false)
    val creditNoteIssuedAt: Instant? = null
) : TimestampedEntity()
