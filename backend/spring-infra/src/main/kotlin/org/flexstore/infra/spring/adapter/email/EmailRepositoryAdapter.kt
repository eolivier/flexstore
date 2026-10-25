package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.email.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.jpa.EmailVerificationEntity
import org.flexstore.infra.spring.adapter.jpa.JpaEmailVerificationRepository
import org.flexstore.infra.spring.adapter.jpa.hash
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The emails of the shop: sent by the EmailSender (SMTP or logs), the verifications kept in PostgreSQL
@Repository
@Transactional
class EmailRepositoryAdapter(
    private val emailSender: EmailSender,
    private val jpaVerifications: JpaEmailVerificationRepository
) : EmailRepository {

    override fun send(email: OrderConfirmationEmail) = emailSender.send(email)

    override fun send(link: EmailVerificationLink) = emailSender.send(link)

    override fun send(email: PasswordResetEmail) = emailSender.send(email)

    override fun save(verification: EmailVerification) {
        val verificationEntity = EmailVerificationEntity(
            Identity.nextId().value,
            tokenHash = verification.token.hash(),
            userId = verification.userId.value,
            expiresAt = verification.expiresAt.value
        )
        jpaVerifications.save(verificationEntity)
    }

    @Transactional(readOnly = true)
    override fun findBy(token: VerificationToken): EmailVerification? =
        jpaVerifications.findByTokenHash(token.hash())
            ?.let { EmailVerification(UserId.ValidUserId(it.userId), token, ExpirationDate(it.expiresAt)) }

    override fun expireAllVerificationsOf(userId: UserId, expiration: ExpirationDate) {
        jpaVerifications.expireAllByUserId(userId.value, expiration.value)
    }

    override fun deleteAllVerificationsOf(userId: UserId) {
        jpaVerifications.deleteAllByUserId(userId.value)
    }
}
