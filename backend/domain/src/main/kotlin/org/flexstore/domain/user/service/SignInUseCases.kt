package org.flexstore.domain.user.service

import org.flexstore.domain.twofactor.usecase.VerifyTwoFactorCodeUseCase
import org.flexstore.domain.user.usecase.LoginUseCase
import org.flexstore.domain.user.usecase.RecordFailedSignInUseCase
import org.flexstore.domain.user.usecase.ResetFailedSignInsUseCase
import org.flexstore.domain.user.usecase.SignInWithGoogleUseCase

// The use cases of a sign-in: the login, the one with Google, the two-factor code, and the count of the failed
// sign-ins
data class SignInUseCases(
    val loginUseCase: LoginUseCase,
    val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    val verifyTwoFactorCodeUseCase: VerifyTwoFactorCodeUseCase,
    val recordFailedSignInUseCase: RecordFailedSignInUseCase,
    val resetFailedSignInsUseCase: ResetFailedSignInsUseCase
)
