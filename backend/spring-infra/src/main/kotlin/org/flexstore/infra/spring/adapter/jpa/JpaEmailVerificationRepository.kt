package org.flexstore.infra.spring.adapter.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository

// The verifications of the email addresses, used by the EmailRepositoryAdapter
@Repository
interface JpaEmailVerificationRepository : JpaRepository<EmailVerificationEntity, String> {
    fun findByTokenHash(tokenHash: String): EmailVerificationEntity?

    @Modifying
    fun deleteAllByUserId(userId: String)
}
