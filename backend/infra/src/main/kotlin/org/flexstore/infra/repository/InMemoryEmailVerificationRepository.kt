package org.flexstore.infra.repository

import org.flexstore.domain.user.*

class InMemoryEmailVerificationRepository : EmailVerificationRepository {

    private val verifications = mutableListOf<EmailVerification>()

    override fun save(verification: EmailVerification) {
        verifications.add(verification)
    }

    override fun findBy(token: VerificationToken): EmailVerification? = verifications.firstOrNull { it.token == token }

    override fun deleteAllOf(userId: UserId) {
        verifications.removeIf { it.userId == userId }
    }
}
