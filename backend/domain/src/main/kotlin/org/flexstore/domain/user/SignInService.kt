package org.flexstore.domain.user

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*

// Domain service: an account signs in. A wrong password is counted, and locks the account at the limit (see
// RecordFailedSignInUseCase); the right password starts the count again. The user repository only builds the
// login use case, which keeps the user it authenticated: one per sign-in, as it is not shared between calls.
class SignInService(
    private val userRepository: UserRepository,
    private val recordFailedSignInUseCase: RecordFailedSignInUseCase,
    private val resetFailedSignInsUseCase: ResetFailedSignInsUseCase
) {

    fun signIn(loginRequest: LoginRequest): User {
        val loginUseCase = LoginUseCase(userRepository)
        try {
            loginUseCase.unfold(loginRequest)
        } catch (wrongPassword: InvalidCredentialsException) {
            recordFailedSignInUseCase.unfold(loginRequest.email)
            throw wrongPassword
        }
        val user = loginUseCase.authenticatedUser
        resetFailedSignInsUseCase.unfold(user.id)
        return user
    }
}
