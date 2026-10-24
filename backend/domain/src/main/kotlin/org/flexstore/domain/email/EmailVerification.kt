package org.flexstore.domain.email

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.model.*
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

// The moment from which a verification link does not work any more
data class ExpirationDate(val value: Instant) {
    fun isReachedAt(instant: Instant) = !instant.isBefore(value)
}

// The verification of the email of an account: the link sent to the customer is valid for 1 hour.
// It holds the rules of the verification: which accounts need one, and what confirming it does
data class EmailVerification(val userId: UserId, val token: VerificationToken, val expiresAt: ExpirationDate) {

    fun isExpiredAt(instant: Instant) = expiresAt.isReachedAt(instant)

    // Confirming the email activates an account that waits for it; any other status (e.g. suspended) is kept
    fun confirm(user: User.DefinedUser): User.DefinedUser =
        if (isNeededFor(user)) user.copy(status = AccountStatus.ACTIVE) else user

    companion object {
        val VALIDITY: Duration = Duration.ofHours(1)

        fun requestedAt(userId: UserId, instant: Instant) =
            EmailVerification(userId, VerificationToken.nextToken(), ExpirationDate(instant + VALIDITY))

        // Only an account that waits for the verification of its email needs a link
        fun isNeededFor(user: User.DefinedUser) = user.status == AccountStatus.PENDING_VERIFICATION

        fun isAlreadyDoneFor(user: User.DefinedUser) = !isNeededFor(user)
    }
}

// The address of the page of the shop that verifies an email (e.g. https://shop.example/verify-email)
data class VerificationPage(val value: String)

data class Link(val value: String)

// The email with the link that verifies the address of a customer; its wording is up to the infrastructure
data class EmailVerificationLink(
    val recipient: Email,
    val customerName: FullName,
    val page: VerificationPage,
    val token: VerificationToken,
    val expiresAt: ExpirationDate
) {
    fun link() = Link("${page.value.trimEnd('/')}?token=${token.value}")
}

data class VerificationLinkUnknownException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class VerificationLinkExpiredException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class AccountAlreadyVerifiedException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
