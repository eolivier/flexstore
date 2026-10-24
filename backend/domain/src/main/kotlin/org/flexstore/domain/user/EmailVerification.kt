package org.flexstore.domain.user

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64

// The secret of a verification link: random, it cannot be guessed
data class VerificationToken(val value: String) {
    companion object {
        private val random = SecureRandom()

        fun nextToken(): VerificationToken {
            val bytes = ByteArray(32).also { random.nextBytes(it) }
            return VerificationToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes))
        }
    }
}

// The verification of the email of an account: the link sent to the customer is valid for 1 hour
// The moment from which a verification link does not work any more
data class ExpirationDate(val value: Instant) {
    fun isReachedAt(instant: Instant) = !instant.isBefore(value)
}

data class EmailVerification(val userId: UserId, val token: VerificationToken, val expiresAt: ExpirationDate) {

    fun isExpiredAt(instant: Instant) = expiresAt.isReachedAt(instant)

    companion object {
        val VALIDITY: Duration = Duration.ofHours(1)

        fun requestedAt(userId: UserId, instant: Instant) = EmailVerification(userId, VerificationToken.nextToken(), ExpirationDate(instant + VALIDITY))
    }
}

// The email with the verification link; its wording and the address of the link are up to the infrastructure
data class EmailVerificationEmail(val recipient: Email, val customerName: FullName, val token: VerificationToken, val expiresAt: ExpirationDate)

data class VerificationLinkUnknownException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class VerificationLinkExpiredException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class AccountAlreadyVerifiedException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
