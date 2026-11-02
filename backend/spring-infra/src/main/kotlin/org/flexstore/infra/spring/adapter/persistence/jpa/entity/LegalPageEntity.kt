package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// A version of a legal page, never changed once saved: the current page of a kind is the one with the highest
// version
@Entity
@Table(name = "legal_page")
data class LegalPageEntity(
    @Id
    val id: String,

    // LEGAL_NOTICE or TERMS_OF_SALE
    @Column(nullable = false, updatable = false)
    val kind: String,

    @Column(nullable = false, updatable = false)
    val version: Int,

    @Column(nullable = false, updatable = false)
    val text: String,

    @Column(name = "published_at", nullable = false, updatable = false)
    val publishedAt: Instant
) : TimestampedEntity()
