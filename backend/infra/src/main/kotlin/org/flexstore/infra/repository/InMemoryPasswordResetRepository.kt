package org.flexstore.infra.repository

import org.flexstore.domain.email.*
import org.flexstore.domain.user.model.UserId

// Keeps the password resets in memory: for the tests
class InMemoryPasswordResetRepository : PasswordResetRepository {

    private val resets = mutableListOf<PasswordReset>()

    override fun save(reset: PasswordReset) {
        resets.add(reset)
    }

    override fun findBy(token: VerificationToken): PasswordReset? =
        resets.firstOrNull { it.expiringToken.token == token }

    override fun deleteAllOf(userId: UserId) {
        resets.removeIf { it.userId == userId }
    }
}
