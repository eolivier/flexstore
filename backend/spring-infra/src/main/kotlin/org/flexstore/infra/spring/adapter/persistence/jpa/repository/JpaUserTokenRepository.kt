package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserTokenEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserTokenType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

// The tokens of the users: the links sent by email (verification of the email, new password), used by the
// EmailRepositoryAdapter and the PostgresPasswordResetRepositoryAdapter, and the sessions of the signed-in
// users, used by the PostgresRefreshSessionRepositoryAdapter
@Repository
interface JpaUserTokenRepository : JpaRepository<UserTokenEntity, String> {
    fun findByTokenHashAndType(tokenHash: String, type: UserTokenType): UserTokenEntity?

    fun existsByTokenHashAndType(tokenHash: String, type: UserTokenType): Boolean

    fun existsByUserIdAndTypeAndRevokedAtIsNull(userId: String, type: UserTokenType): Boolean

    @Modifying
    fun deleteAllByUserIdAndType(userId: String, type: UserTokenType)

    // The links of the user expire at the given date, unless they already expired before
    @Modifying
    @Query(
        "UPDATE UserTokenEntity t SET t.expiresAt = :expiration " +
            "WHERE t.userId = :userId AND t.type = :type AND t.expiresAt > :expiration"
    )
    fun expireAllByUserIdAndType(
        @Param("userId") userId: String,
        @Param("type") type: UserTokenType,
        @Param("expiration") expiration: Instant
    )

    // The sessions of the user that are not revoked yet are revoked at the given moment
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE UserTokenEntity t SET t.revokedAt = :revokedAt " +
            "WHERE t.userId = :userId AND t.type = :type AND t.revokedAt IS NULL"
    )
    fun revokeAllByUserIdAndType(
        @Param("userId") userId: String,
        @Param("type") type: UserTokenType,
        @Param("revokedAt") revokedAt: Instant
    )
}
