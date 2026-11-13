package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.email.model.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.email.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserTokenEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserTokenType
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.usertoken.JpaUserTokenRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.hash
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The emails of the shop: sent by the EmailSender (SMTP or logs), the verifications kept in PostgreSQL
@Repository
@Transactional
class EmailRepositoryAdapter(
    private val emailSender: EmailSender,
    private val jpaVerifications: JpaUserTokenRepository
) : EmailRepository {

    override fun send(email: OrderConfirmationEmail) = emailSender.send(email)

    override fun send(email: OrderStatusEmail) = emailSender.send(email)

    override fun send(link: EmailVerificationLink) = emailSender.send(link)

    override fun send(email: PasswordResetEmail) = emailSender.send(email)

    override fun send(email: AbandonedCartReminderEmail) = emailSender.send(email)

    override fun save(verification: EmailVerification) {
        val verificationEntity = UserTokenEntity(
            Identity.nextId().value,
            type = UserTokenType.EMAIL_VERIFICATION,
            tokenHash = verification.token.hash(),
            userId = verification.userId.value,
            expiresAt = verification.expiresAt.value
        )
        jpaVerifications.save(verificationEntity)
    }

    @Transactional(readOnly = true)
    override fun findBy(token: VerificationToken): EmailVerification? =
        jpaVerifications.findByTokenHashAndType(token.hash(), UserTokenType.EMAIL_VERIFICATION)
            ?.let { EmailVerification(UserId.ValidUserId(it.userId), token, ExpirationDate(it.expiresAt)) }

    override fun expireAllVerificationsOf(userId: UserId, expiration: ExpirationDate) {
        jpaVerifications.expireAllByUserIdAndType(
            userId.value, UserTokenType.EMAIL_VERIFICATION, expiration.value
        )
    }

    override fun deleteAllVerificationsOf(userId: UserId) {
        jpaVerifications.deleteAllByUserIdAndType(userId.value, UserTokenType.EMAIL_VERIFICATION)
    }
}
