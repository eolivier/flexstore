package org.flexstore.domain.user.repository

import org.flexstore.domain.user.model.*

// The sessions of the signed-in users, revoked ones included: a revoked refresh token that comes back is a
// replay
interface RefreshSessionRepository {

    // Keeps a new session, or the revocation of a stored one
    fun save(session: RefreshSession)

    fun isKnown(token: RefreshToken): Boolean

    fun isUnknown(token: RefreshToken): Boolean = !isKnown(token)

    // Only called once the token is known
    fun get(token: RefreshToken): RefreshSession

    // Ends every open session of the user, in one operation
    fun revokeAllOf(userId: UserId, sessionMoment: SessionMoment)

    fun hasOpenSessionOf(userId: UserId): Boolean
}
