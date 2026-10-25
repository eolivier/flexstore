package org.flexstore.domain.user

import org.flexstore.domain.user.model.Email
import org.flexstore.domain.user.usecase.*
import java.time.Clock

// Domain service: a customer who forgot their password receives a link (valid for 1 hour) and chooses a new one
class PasswordResetService(
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val clock: Clock = Clock.systemUTC()
) {

    fun requestReset(email: Email) =
        requestPasswordResetUseCase.unfold(PasswordResetRequest(email, clock.instant()))

    fun reset(chosenPassword: ChosenPassword) =
        resetPasswordUseCase.unfold(PasswordResetConfirmation(chosenPassword, clock.instant()))
}
