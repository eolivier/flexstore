package org.flexstore.domain.user

import org.flexstore.domain.email.VerificationToken
import org.flexstore.domain.user.usecase.*
import java.time.Clock

// Domain service: a customer creates their account, receives a verification link (valid for 1 hour) and opens it
class CustomerRegistrationService(
    private val registerCustomerUseCase: RegisterCustomerUseCase,
    private val sendVerificationEmailUseCase: SendVerificationEmailUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val readAccountUseCase: ReadAccountUseCase,
    private val clock: Clock = Clock.systemUTC()
) {

    // The account is read back with the id chosen for it
    fun register(registration: CustomerRegistration): Account {
        registerCustomerUseCase.unfold(registration)
        sendVerificationEmail(registration.userId)
        return readAccountUseCase.getAccount(registration.userId)
    }

    // A new link, when the previous one has expired or was lost
    fun sendVerificationEmail(userId: UserId) =
        sendVerificationEmailUseCase.unfold(VerificationEmailRequest(userId, clock.instant()))

    fun verifyEmail(token: VerificationToken) =
        verifyEmailUseCase.unfold(EmailVerificationConfirmation(token, clock.instant()))
}
