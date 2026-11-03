package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// The choice of a customer about the preference cookies, valid until its expiry
@Entity
@Table(name = "cookie_consent")
data class CookieConsentEntity(
    @Id
    val id: String,

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    val userId: String,

    // ACCEPTED or REFUSED
    @Column(nullable = false)
    val choice: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant
) : TimestampedEntity()
