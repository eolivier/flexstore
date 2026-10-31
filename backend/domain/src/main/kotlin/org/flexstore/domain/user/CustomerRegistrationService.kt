package org.flexstore.domain.user

import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.user.model.*
import org.flexstore.domain.email.VerificationToken
import org.flexstore.domain.user.usecase.*

// Domain service: a customer creates their account, receives a verification link (valid for 1 hour) and opens it
class CustomerRegistrationService(
    private val registerCustomerUseCase: RegisterCustomerUseCase,
    private val sendVerificationEmailUseCase: SendVerificationEmailUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val readAccountUseCase: ReadAccountUseCase
) {

    // The account is read back with the id chosen for it
    fun register(registration: CustomerRegistration): Account {
        registerCustomerUseCase.unfold(registration)
        sendVerificationEmail(registration.userId)
        return readAccountUseCase.unfold(registration.userId)
    }

    fun getAccount(userId: UserId): Account = readAccountUseCase.unfold(userId)

    // A new link, when the previous one has expired or was lost
    fun sendVerificationEmail(userId: UserId) =
        sendVerificationEmailUseCase.unfold(VerificationEmailRequest(userId, InstantProvider.now()))

    fun verifyEmail(token: VerificationToken) =
        verifyEmailUseCase.unfold(EmailVerificationConfirmation(token, InstantProvider.now()))
}
