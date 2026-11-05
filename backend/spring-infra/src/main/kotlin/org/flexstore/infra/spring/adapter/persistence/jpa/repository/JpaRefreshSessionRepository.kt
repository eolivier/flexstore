package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.repository.RefreshSessionRepository
import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.RefreshSessionEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.hash
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
interface JpaRefreshSessionRepository : JpaRepository<RefreshSessionEntity, String> {
    fun findByTokenHash(tokenHash: String): RefreshSessionEntity?

    fun existsByTokenHash(tokenHash: String): Boolean

    fun existsByUserIdAndRevokedAtIsNull(userId: String): Boolean

    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE RefreshSessionEntity s SET s.revokedAt = :revokedAt " +
            "WHERE s.userId = :userId AND s.revokedAt IS NULL"
    )
    fun revokeAllByUserId(@Param("userId") userId: String, @Param("revokedAt") revokedAt: Instant)
}

// One row per session: renewing or ending a session updates its row, the row of the next session is inserted
@Repository
@Transactional
class PostgresRefreshSessionRepositoryAdapter(private val jpaRepo: JpaRefreshSessionRepository) :
    RefreshSessionRepository {

    override fun save(session: RefreshSession) {
        val tokenHash = session.refreshToken.hash()
        val storedId = jpaRepo.findByTokenHash(tokenHash)?.id ?: Identity.nextId().value
        val revokedAt = when (val revocation = session.revocation) {
            SessionRevocation.NotRevoked -> null
            is SessionRevocation.RevokedAt -> revocation.sessionMoment.value
        }
        jpaRepo.save(
            RefreshSessionEntity(
                id = storedId,
                tokenHash = tokenHash,
                userId = session.userId.value,
                expiresAt = session.expiry.value,
                revokedAt = revokedAt
            )
        )
    }

    @Transactional(readOnly = true)
    override fun isUnknown(token: RefreshToken): Boolean = !jpaRepo.existsByTokenHash(token.hash())

    @Transactional(readOnly = true)
    override fun get(token: RefreshToken): RefreshSession {
        val entity = jpaRepo.findByTokenHash(token.hash())
            ?: throw NoSuchElementException("Refresh session not found")
        val revocation = entity.revokedAt?.let { SessionRevocation.RevokedAt(SessionMoment(it)) }
            ?: SessionRevocation.NotRevoked
        val userId = UserId.ValidUserId(entity.userId)
        return RefreshSession(userId, token, SessionExpiry(entity.expiresAt), revocation)
    }

    override fun revokeAllOf(userId: UserId, sessionMoment: SessionMoment) {
        jpaRepo.revokeAllByUserId(userId.value, sessionMoment.value)
    }

    @Transactional(readOnly = true)
    override fun hasOpenSessionOf(userId: UserId): Boolean =
        jpaRepo.existsByUserIdAndRevokedAtIsNull(userId.value)
}
