package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.email.*
import org.flexstore.domain.user.UserId
import org.flexstore.infra.spring.adapter.jpa.EmailVerificationEntity
import org.flexstore.infra.spring.adapter.jpa.JpaEmailVerificationRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.util.HexFormat

// The emails of the shop: sent by the EmailSender (SMTP or logs), the verifications kept in PostgreSQL
@Repository
@Transactional
class EmailRepositoryAdapter(
    private val emailSender: EmailSender,
    private val jpaVerifications: JpaEmailVerificationRepository
) : EmailRepository {

    override fun send(email: OrderConfirmationEmail) = emailSender.send(email)

    override fun send(link: EmailVerificationLink) = emailSender.send(link)

    override fun save(verification: EmailVerification) {
        jpaVerifications.save(EmailVerificationEntity(hash(verification.token), verification.userId.value, verification.expiresAt.value))
    }

    @Transactional(readOnly = true)
    override fun findBy(token: VerificationToken): EmailVerification? =
        jpaVerifications.findById(hash(token))
            .map { EmailVerification(UserId.ValidUserId(it.userId), token, ExpirationDate(it.expiresAt)) }
            .orElse(null)

    override fun deleteAllVerificationsOf(userId: UserId) {
        jpaVerifications.deleteAllByUserId(userId.value)
    }

    // The token is the secret of the link: the database only keeps its hash, and the token is found back by hashing it
    private fun hash(token: VerificationToken): String =
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.value.toByteArray()))
}
