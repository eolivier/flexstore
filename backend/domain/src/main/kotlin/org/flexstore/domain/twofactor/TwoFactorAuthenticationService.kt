package org.flexstore.domain.twofactor

import org.flexstore.domain.twofactor.model.ProvisioningUri
import org.flexstore.domain.twofactor.model.TwoFactorCode
import org.flexstore.domain.twofactor.model.TwoFactorIssuer
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.twofactor.usecase.AdminDisableTwoFactorAuthenticationUseCase
import org.flexstore.domain.twofactor.usecase.AdminTwoFactorDisabling
import org.flexstore.domain.twofactor.usecase.ConfirmTwoFactorEnrollmentUseCase
import org.flexstore.domain.twofactor.usecase.DisableTwoFactorAuthenticationUseCase
import org.flexstore.domain.twofactor.usecase.StartTwoFactorEnrollmentUseCase
import org.flexstore.domain.twofactor.usecase.TwoFactorDisabling
import org.flexstore.domain.twofactor.usecase.TwoFactorEnrollmentConfirmation
import org.flexstore.domain.twofactor.usecase.TwoFactorEnrollmentStart
import org.flexstore.domain.user.model.PlainPassword
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.entity.Actor
import org.flexstore.domain.user.usecase.ReadAccountUseCase

// What a customer or administrator reads once an enrollment started: the secret to type manually, and the
// link a QR code is generated from
data class TwoFactorEnrollment(val secret: TwoFactorSecret, val provisioningUri: ProvisioningUri)

// Domain service: a signed-in customer or administrator sets up or removes the two-factor authentication of
// their own account; an administrator also removes it from another user's account, from the user
// administration screen
class TwoFactorAuthenticationService(
    private val readAccountUseCase: ReadAccountUseCase,
    private val startTwoFactorEnrollmentUseCase: StartTwoFactorEnrollmentUseCase,
    private val confirmTwoFactorEnrollmentUseCase: ConfirmTwoFactorEnrollmentUseCase,
    private val disableTwoFactorAuthenticationUseCase: DisableTwoFactorAuthenticationUseCase,
    private val adminDisableTwoFactorAuthenticationUseCase: AdminDisableTwoFactorAuthenticationUseCase
) {
    fun startEnrollment(customer: Actor): TwoFactorEnrollment {
        val secret = TwoFactorSecret.next()
        startTwoFactorEnrollmentUseCase.unfold(TwoFactorEnrollmentStart(customer, secret))
        val accountName = readAccountUseCase.unfold(customer.customerId()).email
        return TwoFactorEnrollment(secret, secret.provisioningUri(accountName, TwoFactorIssuer.flexstore()))
    }

    fun confirmEnrollment(customer: Actor, code: TwoFactorCode) =
        confirmTwoFactorEnrollmentUseCase.unfold(TwoFactorEnrollmentConfirmation(customer, code))

    fun disable(customer: Actor, currentPassword: PlainPassword) =
        disableTwoFactorAuthenticationUseCase.unfold(TwoFactorDisabling(customer, currentPassword))

    fun disableForUser(editor: Actor, userId: UserId) =
        adminDisableTwoFactorAuthenticationUseCase.unfold(AdminTwoFactorDisabling(editor, userId))
}
