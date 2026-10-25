package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*
import java.time.Instant

// The link to choose a new password, last asked by a user
@Entity
@Table(name = "password_reset")
data class PasswordResetEntity(
    @Id
    val id: String,

    // SHA-256 of the token sent by email, never the token itself
    @Column(name = "token_hash", nullable = false, unique = true, updatable = false)
    val tokenHash: String,

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    val userId: String,

    @Column(name = "expires_at", nullable = false, updatable = false)
    val expiresAt: Instant
) : TimestampedEntity()
