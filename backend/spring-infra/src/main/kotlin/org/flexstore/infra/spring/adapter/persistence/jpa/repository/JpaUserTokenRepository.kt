package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserTokenEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserTokenType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

// The links sent by email (verification of the email, new password), used by the EmailRepositoryAdapter and the
// PostgresPasswordResetRepositoryAdapter
@Repository
interface JpaUserTokenRepository : JpaRepository<UserTokenEntity, String> {
    fun findByTokenHashAndType(tokenHash: String, type: UserTokenType): UserTokenEntity?

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
}
