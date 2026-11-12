package org.flexstore.domain.user.model

import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64
import org.flexstore.domain.sharedkernel.InstantProvider
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.PreConditionException

// The secret that renews the access of a signed-in user once their access token expired: random, it cannot be
// guessed. The database only keeps its hash
data class RefreshToken(val value: String) {
    companion object {
        private const val TOKEN_BYTES = 32
        private val random = SecureRandom()

        fun nextToken(): RefreshToken {
            val bytes = ByteArray(TOKEN_BYTES).also { random.nextBytes(it) }
            return RefreshToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes))
        }
    }
}

// A moment of a session: when it is opened or renewed, when it is ended, when it is checked
data class SessionMoment(val value: Instant) {
    companion object {
        fun now() = SessionMoment(InstantProvider.now())
    }
}

// The moment from which a session cannot be renewed any more
data class SessionExpiry(val value: Instant) {
    fun isReachedAt(sessionMoment: SessionMoment) = sessionMoment.value >= value
}

// A session that was renewed or ended, or that is still open
sealed interface SessionRevocation {
    data object NotRevoked : SessionRevocation
    data class RevokedAt(val sessionMoment: SessionMoment) : SessionRevocation
}

// A session of a signed-in user: its refresh token is used once to get a new access token and a new refresh
// token, then it is revoked (the token is rotated). A revoked refresh token that comes back was stolen or
// replayed: all the sessions of the user are ended
data class RefreshSession(
    val userId: UserId,
    val refreshToken: RefreshToken,
    val expiry: SessionExpiry,
    val revocation: SessionRevocation = SessionRevocation.NotRevoked
) {

    fun isRevoked() = revocation is SessionRevocation.RevokedAt

    fun isOpen() = !isRevoked()

    fun isExpiredAt(sessionMoment: SessionMoment) = expiry.isReachedAt(sessionMoment)

    fun revokedAt(sessionMoment: SessionMoment) = copy(revocation = SessionRevocation.RevokedAt(sessionMoment))

    companion object {
        private const val VALIDITY_DAYS = 7L

        // A session lasts 7 days, as long as it is not revoked
        val VALIDITY: Duration = Duration.ofDays(VALIDITY_DAYS)

        fun openedAt(userId: UserId, sessionMoment: SessionMoment): RefreshSession {
            val expiry = SessionExpiry(sessionMoment.value + VALIDITY)
            return RefreshSession(userId, RefreshToken.nextToken(), expiry)
        }
    }
}

// A user who has just been signed in: their account, and the session that renews their access
data class SignedInSession(val account: Account, val refreshSession: RefreshSession)

// A session that is renewed: the refresh token presented, the session that replaces it and the moment
data class SessionRotation(
    val presented: RefreshToken,
    val next: RefreshSession,
    val sessionMoment: SessionMoment
)

// A session that is ended: the refresh token presented and the moment
data class SessionEnding(val presented: RefreshToken, val sessionMoment: SessionMoment)

// All the sessions of a user are ended
data class AllSessionsEnding(val userId: UserId, val sessionMoment: SessionMoment)

data class RefreshTokenUnknownException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class RefreshTokenExpiredException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class RefreshTokenReusedException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class RefreshSessionFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
