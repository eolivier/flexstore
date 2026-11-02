package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// The credit note of a refunded order, which cancels its invoice, numbered in the sequence of its year
// (credit_note_counter)
@Entity
@Table(name = "credit_note")
data class CreditNoteEntity(
    @Id
    val id: String,

    @Column(nullable = false, unique = true, updatable = false)
    val number: String,

    @Column(name = "invoice_id", nullable = false, unique = true, updatable = false)
    val invoiceId: String,

    @Column(name = "issued_at", nullable = false, updatable = false)
    val issuedAt: Instant
) : TimestampedEntity()
