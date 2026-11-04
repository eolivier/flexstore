package org.flexstore.domain.twofactor

import org.flexstore.domain.twofactor.model.ProvisioningUri
import org.flexstore.domain.twofactor.model.TwoFactorCode
import org.flexstore.domain.twofactor.model.TwoFactorIssuer
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.twofactor.usecase.ConfirmTwoFactorEnrollmentUseCase
import org.flexstore.domain.twofactor.usecase.DisableTwoFactorAuthenticationUseCase
import org.flexstore.domain.twofactor.usecase.StartTwoFactorEnrollmentUseCase
import org.flexstore.domain.twofactor.usecase.TwoFactorDisabling
import org.flexstore.domain.twofactor.usecase.TwoFactorEnrollmentConfirmation
import org.flexstore.domain.twofactor.usecase.TwoFactorEnrollmentStart
import org.flexstore.domain.user.model.PlainPassword
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.usecase.ReadAccountUseCase

// What an administrator reads once an enrollment started: the secret to type manually, and the link a QR code
// is generated from
data class TwoFactorEnrollment(val secret: TwoFactorSecret, val provisioningUri: ProvisioningUri)

// Domain service: an administrator sets up or removes the two-factor authentication of their own account
class TwoFactorAuthenticationService(
    private val readAccountUseCase: ReadAccountUseCase,
    private val startTwoFactorEnrollmentUseCase: StartTwoFactorEnrollmentUseCase,
    private val confirmTwoFactorEnrollmentUseCase: ConfirmTwoFactorEnrollmentUseCase,
    private val disableTwoFactorAuthenticationUseCase: DisableTwoFactorAuthenticationUseCase
) {
    fun startEnrollment(admin: UserId): TwoFactorEnrollment {
        val secret = TwoFactorSecret.next()
        startTwoFactorEnrollmentUseCase.unfold(TwoFactorEnrollmentStart(admin, secret))
        val accountName = readAccountUseCase.unfold(admin).email
        return TwoFactorEnrollment(secret, secret.provisioningUri(accountName, TwoFactorIssuer.flexstore()))
    }

    fun confirmEnrollment(admin: UserId, code: TwoFactorCode) =
        confirmTwoFactorEnrollmentUseCase.unfold(TwoFactorEnrollmentConfirmation(admin, code))

    fun disable(admin: UserId, currentPassword: PlainPassword) =
        disableTwoFactorAuthenticationUseCase.unfold(TwoFactorDisabling(admin, currentPassword))
}
