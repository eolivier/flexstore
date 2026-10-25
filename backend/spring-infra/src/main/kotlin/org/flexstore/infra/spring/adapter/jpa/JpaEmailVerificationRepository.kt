package org.flexstore.infra.spring.adapter.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

// The verifications of the email addresses, used by the EmailRepositoryAdapter
@Repository
interface JpaEmailVerificationRepository : JpaRepository<EmailVerificationEntity, String> {
    fun findByTokenHash(tokenHash: String): EmailVerificationEntity?

    @Modifying
    fun deleteAllByUserId(userId: String)

    // The links of the user expire at the given date, unless they already expired before
    @Modifying
    @Query(
        "UPDATE EmailVerificationEntity v SET v.expiresAt = :expiration " +
            "WHERE v.userId = :userId AND v.expiresAt > :expiration"
    )
    fun expireAllByUserId(@Param("userId") userId: String, @Param("expiration") expiration: Instant)
}
