package org.flexstore.domain.user

import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.user.model.Email
import org.flexstore.domain.user.usecase.*

// Domain service: a customer who forgot their password receives a link (valid for 1 hour) and chooses a new one
class PasswordResetService(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) {

    fun requestReset(email: Email) =
        requestPasswordResetUseCase.unfold(PasswordResetRequest(email, InstantProvider.now()))

    fun reset(chosenPassword: ChosenPassword) =
        resetPasswordUseCase.unfold(PasswordResetConfirmation(chosenPassword, InstantProvider.now()))
}
