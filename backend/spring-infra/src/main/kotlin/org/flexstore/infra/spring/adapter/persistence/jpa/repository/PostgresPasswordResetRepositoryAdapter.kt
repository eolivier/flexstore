package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.email.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserTokenEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserTokenType
import org.flexstore.infra.spring.adapter.persistence.jpa.hash
import org.flexstore.domain.email.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class PostgresPasswordResetRepositoryAdapter(private val jpaRepo: JpaUserTokenRepository) :
    PasswordResetRepository {

    override fun save(reset: PasswordReset) {
        val expiringToken = reset.expiringToken
        val resetEntity = UserTokenEntity(
            Identity.nextId().value,
            type = UserTokenType.PASSWORD_RESET,
            tokenHash = expiringToken.token.hash(),
            userId = reset.userId.value,
            expiresAt = expiringToken.expiresAt.value
        )
        jpaRepo.save(resetEntity)
    }

    @Transactional(readOnly = true)
    override fun findBy(token: VerificationToken): PasswordReset? =
        jpaRepo.findByTokenHashAndType(token.hash(), UserTokenType.PASSWORD_RESET)?.let { resetEntity ->
            val expiringToken = ExpiringToken(token, ExpirationDate(resetEntity.expiresAt))
            PasswordReset(UserId.ValidUserId(resetEntity.userId), expiringToken)
        }

    // Flushed at once, so that the new link of the user can be inserted in the same transaction
    override fun deleteAllOf(userId: UserId) {
        jpaRepo.deleteAllByUserIdAndType(userId.value, UserTokenType.PASSWORD_RESET)
        jpaRepo.flush()
    }
}
