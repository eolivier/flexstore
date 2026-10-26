package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.PasswordResetEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.hash
import org.flexstore.domain.email.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaPasswordResetRepository : JpaRepository<PasswordResetEntity, String> {
    fun findByTokenHash(tokenHash: String): PasswordResetEntity?

    @Modifying
    fun deleteAllByUserId(userId: String)
}

@Repository
@Transactional
class PostgresPasswordResetRepositoryAdapter(private val jpaRepo: JpaPasswordResetRepository) :
    PasswordResetRepository {

    override fun save(reset: PasswordReset) {
        val expiringToken = reset.expiringToken
        val resetEntity = PasswordResetEntity(
            Identity.nextId().value,
            tokenHash = expiringToken.token.hash(),
            userId = reset.userId.value,
            expiresAt = expiringToken.expiresAt.value
        )
        jpaRepo.save(resetEntity)
    }

    @Transactional(readOnly = true)
    override fun findBy(token: VerificationToken): PasswordReset? =
        jpaRepo.findByTokenHash(token.hash())?.let { resetEntity ->
            val expiringToken = ExpiringToken(token, ExpirationDate(resetEntity.expiresAt))
            PasswordReset(UserId.ValidUserId(resetEntity.userId), expiringToken)
        }

    // Flushed at once, so that the new link of the user can be inserted in the same transaction
    override fun deleteAllOf(userId: UserId) {
        jpaRepo.deleteAllByUserId(userId.value)
        jpaRepo.flush()
    }
}
