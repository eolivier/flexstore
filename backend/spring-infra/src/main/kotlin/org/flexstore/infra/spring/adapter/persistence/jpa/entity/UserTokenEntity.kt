package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// The operation a token of a user is for: a link sent by email, or the session of a signed-in user
enum class UserTokenType { EMAIL_VERIFICATION, PASSWORD_RESET, REFRESH_SESSION }

// A secret token of a user, kept by its hash: the type tells the operation (a user may have several verification
// links over time, only the last password reset link asked, and several sessions at once)
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
    val expiresAt: Instant,

    // The moment a session was revoked, empty until then; always empty for the links sent by email
    @Column(name = "revoked_at")
    val revokedAt: Instant? = null
) : TimestampedEntity()
