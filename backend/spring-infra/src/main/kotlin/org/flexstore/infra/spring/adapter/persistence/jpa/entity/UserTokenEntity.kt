package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// The operation a link sent by email to a user is for
enum class UserTokenType { EMAIL_VERIFICATION, PASSWORD_RESET }

// A link sent by email to a user: the type tells the operation (a user may have several verification links over
// time, and only the last password reset link asked)
@Entity
@Table(name = "user_token")
data class UserTokenEntity(
    @Id
    val id: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    val type: UserTokenType,

    // SHA-256 of the token sent by email, never the token itself
    @Column(name = "token_hash", nullable = false, unique = true, updatable = false)
    val tokenHash: String,

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: String,

    // Brought forward when a newer verification link is sent
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant
) : TimestampedEntity()
