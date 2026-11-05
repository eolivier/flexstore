package org.flexstore.domain.user.service

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*

// Domain service: the sessions of the signed-in users. A refresh token works once: it is replaced by another one
// at each use, and when a token that was already used comes back, every session of its user is ended before the
// refusal is given. The moment is read once, when the journey starts
class UserSessionService(
    private val openSessionUseCase: OpenSessionUseCase,
    private val readRefreshSessionUseCase: ReadRefreshSessionUseCase,
    private val rotateRefreshSessionUseCase: RotateRefreshSessionUseCase,
    private val revokeRefreshSessionUseCase: RevokeRefreshSessionUseCase,
    private val revokeAllRefreshSessionsUseCase: RevokeAllRefreshSessionsUseCase,
    private val readAccountUseCase: ReadAccountUseCase
) {

    // The session of a user who has just signed in
    fun open(userId: UserId): RefreshSession {
        val session = RefreshSession.openedAt(userId, SessionMoment.now())
        openSessionUseCase.unfold(session)
        return session
    }

    // The new session of the user, in place of the one of the refresh token presented
    fun refresh(presented: RefreshToken): SignedInSession {
        val sessionMoment = SessionMoment.now()
        val current = readRefreshSessionUseCase.unfold(presented)
        val next = RefreshSession.openedAt(current.userId, sessionMoment)
        try {
            rotateRefreshSessionUseCase.unfold(SessionRotation(presented, next, sessionMoment))
        } catch (reuse: RefreshTokenReusedException) {
            revokeAllRefreshSessionsUseCase.unfold(AllSessionsEnding(current.userId, sessionMoment))
            throw reuse
        }
        return SignedInSession(readAccountUseCase.unfold(current.userId), next)
    }

    // The user signs out
    fun close(presented: RefreshToken) {
        revokeRefreshSessionUseCase.unfold(SessionEnding(presented, SessionMoment.now()))
    }
}
