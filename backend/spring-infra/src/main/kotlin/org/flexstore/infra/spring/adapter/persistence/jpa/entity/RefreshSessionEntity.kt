package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// A session of a signed-in user: its refresh token (by hash), until when it works, and when it was revoked
@Entity
@Table(name = "refresh_session")
data class RefreshSessionEntity(
    @Id
    val id: String,

    // SHA-256 of the refresh token given to the user, never the token itself
    @Column(name = "token_hash", nullable = false, unique = true, updatable = false)
    val tokenHash: String,

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: String,

    @Column(name = "expires_at", nullable = false, updatable = false)
    val expiresAt: Instant,

    // Not revoked while it is empty
    @Column(name = "revoked_at")
    val revokedAt: Instant? = null
) : TimestampedEntity()
