package org.flexstore.infra.repository

import org.flexstore.domain.user.repository.RefreshSessionRepository
import org.flexstore.domain.user.model.*

// Keeps the sessions in memory: for the tests
class InMemoryRefreshSessionRepository : RefreshSessionRepository {

    private val sessions = mutableMapOf<RefreshToken, RefreshSession>()

    override fun save(session: RefreshSession) {
        sessions[session.refreshToken] = session
    }

    override fun isUnknown(token: RefreshToken): Boolean = !sessions.containsKey(token)

    override fun get(token: RefreshToken): RefreshSession = sessions.getValue(token)

    override fun revokeAllOf(userId: UserId, sessionMoment: SessionMoment) {
        sessions.replaceAll { _, session ->
            if (session.userId == userId && session.isOpen()) session.revokedAt(sessionMoment) else session
        }
    }

    override fun hasOpenSessionOf(userId: UserId): Boolean =
        sessions.values.any { session -> session.userId == userId && session.isOpen() }
}
