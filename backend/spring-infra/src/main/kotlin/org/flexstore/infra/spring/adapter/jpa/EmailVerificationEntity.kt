package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "email_verification")
data class EmailVerificationEntity(
    // SHA-256 of the token sent by email, never the token itself
    @Id
    @Column(name = "token_hash")
    val tokenHash: String,

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: String,

    @Column(name = "expires_at", nullable = false, updatable = false)
    val expiresAt: Instant
) : TimestampedEntity()
