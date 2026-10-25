package org.flexstore.domain.email

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.sharedkernel.ShopUrl
import org.flexstore.domain.user.model.*
import org.ucop.domain.NominalException
import java.time.Duration
import java.time.Instant

// The secret of a link and the moment from which it does not work any more
data class ExpiringToken(val token: VerificationToken, val expiresAt: ExpirationDate) {
    fun isExpiredAt(instant: Instant) = expiresAt.isReachedAt(instant)
}

// The reset of the password of a customer who forgot it: the link sent by email is valid for 1 hour, and only
// the last link asked works
data class PasswordReset(val userId: UserId, val expiringToken: ExpiringToken) {

    fun isExpiredAt(instant: Instant) = expiringToken.isExpiredAt(instant)

    companion object {
        val VALIDITY: Duration = Duration.ofHours(1)

        fun requestedAt(userId: UserId, instant: Instant): PasswordReset {
            val expiringToken = ExpiringToken(VerificationToken.nextToken(), ExpirationDate(instant + VALIDITY))
            return PasswordReset(userId, expiringToken)
        }
    }
}

// The page of the shop where a customer chooses a new password (e.g. https://shop.example/reset-password)
data class PasswordResetPage(val value: String) {

    fun linkFor(token: VerificationToken) = Link("${value.trimEnd('/')}?token=${token.value}")

    companion object {
        fun of(shopUrl: ShopUrl) = PasswordResetPage(shopUrl.pageAt("reset-password"))
    }
}

// The email with the link that lets a customer choose a new password; its wording is PasswordResetEmailContent
data class PasswordResetEmail(val user: User.DefinedUser, val reset: PasswordReset) {
    val recipient get() = user.email
}

data class PasswordResetLinkUnknownException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class PasswordResetLinkExpiredException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class PasswordResetFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
