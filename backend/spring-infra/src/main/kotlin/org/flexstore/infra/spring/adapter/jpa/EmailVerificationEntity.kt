package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*
import java.time.Instant

// A verification link sent to a user (a user may have several over time)
@Entity
@Table(name = "email_verification")
data class EmailVerificationEntity(
    @Id
    val id: String,

    // SHA-256 of the token sent by email, never the token itself
    @Column(name = "token_hash", nullable = false, unique = true, updatable = false)
    val tokenHash: String,

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: String,

    // Brought forward when a newer link is sent
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant
) : TimestampedEntity()
