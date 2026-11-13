package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.repository.RefreshSessionRepository
import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserTokenEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserTokenType
import org.flexstore.infra.spring.adapter.persistence.jpa.hash
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The sessions are user tokens of the type REFRESH_SESSION, one row per session: renewing or ending a session
// updates its row, the row of the next session is inserted
@Repository
@Transactional
class PostgresRefreshSessionRepositoryAdapter(private val jpaRepo: JpaUserTokenRepository) :
    RefreshSessionRepository {

    override fun save(session: RefreshSession) {
        val tokenHash = session.refreshToken.hash()
        val storedId = jpaRepo.findByTokenHashAndType(tokenHash, SESSION)?.id ?: Identity.nextId().value
        val revokedAt = when (val revocation = session.revocation) {
            SessionRevocation.NotRevoked -> null
            is SessionRevocation.RevokedAt -> revocation.sessionMoment.value
        }
        jpaRepo.save(
            UserTokenEntity(
                id = storedId,
                type = SESSION,
                tokenHash = tokenHash,
                userId = session.userId.value,
                expiresAt = session.expiry.value,
                revokedAt = revokedAt
            )
        )
    }

    @Transactional(readOnly = true)
    override fun isUnknown(token: RefreshToken): Boolean =
        !jpaRepo.existsByTokenHashAndType(token.hash(), SESSION)

    @Transactional(readOnly = true)
    override fun get(token: RefreshToken): RefreshSession {
        val entity = jpaRepo.findByTokenHashAndType(token.hash(), SESSION)
            ?: throw NoSuchElementException("Refresh session not found")
        val revocation = entity.revokedAt?.let { SessionRevocation.RevokedAt(SessionMoment(it)) }
            ?: SessionRevocation.NotRevoked
        val userId = UserId.ValidUserId(entity.userId)
        return RefreshSession(userId, token, SessionExpiry(entity.expiresAt), revocation)
    }

    override fun revokeAllOf(userId: UserId, sessionMoment: SessionMoment) {
        jpaRepo.revokeAllByUserIdAndType(userId.value, SESSION, sessionMoment.value)
    }

    @Transactional(readOnly = true)
    override fun hasOpenSessionOf(userId: UserId): Boolean =
        jpaRepo.existsByUserIdAndTypeAndRevokedAtIsNull(userId.value, SESSION)
}

private val SESSION = UserTokenType.REFRESH_SESSION
