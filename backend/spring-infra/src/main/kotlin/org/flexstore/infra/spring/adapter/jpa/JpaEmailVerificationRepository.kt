package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.user.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.util.HexFormat

@Repository
interface JpaEmailVerificationRepository : JpaRepository<EmailVerificationEntity, String> {
    @Modifying
    fun deleteAllByUserId(userId: String)
}

@Repository
@Transactional
class PostgresEmailVerificationRepositoryAdapter(
    private val jpaRepo: JpaEmailVerificationRepository
) : EmailVerificationRepository {

    // The token is the secret of the link: the database only keeps its hash, and the token is found back by hashing it
    private fun hash(token: VerificationToken): String =
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.value.toByteArray()))

    override fun save(verification: EmailVerification) {
        jpaRepo.save(EmailVerificationEntity(hash(verification.token), verification.userId.value, verification.expiresAt.value))
    }

    @Transactional(readOnly = true)
    override fun findBy(token: VerificationToken): EmailVerification? =
        jpaRepo.findById(hash(token))
            .map { EmailVerification(UserId.ValidUserId(it.userId), token, ExpirationDate(it.expiresAt)) }
            .orElse(null)

    override fun deleteAllOf(userId: UserId) {
        jpaRepo.deleteAllByUserId(userId.value)
    }
}
