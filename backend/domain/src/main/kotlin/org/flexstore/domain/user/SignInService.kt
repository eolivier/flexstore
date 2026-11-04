package org.flexstore.domain.user

import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.domain.twofactor.TwoFactorCodeInvalidException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorChallenge
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorChallengeLookup
import org.flexstore.domain.twofactor.usecase.TwoFactorCodeVerification
import org.flexstore.domain.twofactor.usecase.VerifyTwoFactorCodeUseCase
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*

// Domain service: an account signs in. A wrong password, or a wrong two-factor code, is counted and locks the
// account at the limit (see RecordFailedSignInUseCase); a successful sign-in starts the count again. The user
// repository only builds the login and the two-factor verification use cases, which keep the user they
// authenticated: one per sign-in, as they are not shared between calls.
class SignInService(
    private val userRepository: UserRepository,
    private val recordFailedSignInUseCase: RecordFailedSignInUseCase,
    private val resetFailedSignInsUseCase: ResetFailedSignInsUseCase,
    private val twoFactorChallengeRepository: TwoFactorChallengeRepository
) {

    // An administrator who enabled two-factor authentication is given a challenge instead of a session
    fun signIn(loginRequest: LoginRequest): SignInOutcome {
        val loginUseCase = LoginUseCase(userRepository)
        try {
            loginUseCase.unfold(loginRequest)
        } catch (wrongPassword: InvalidCredentialsException) {
            recordFailedSignInUseCase.unfold(loginRequest.email)
            throw wrongPassword
        }
        val user = loginUseCase.authenticatedUser as User.DefinedUser
        return if (user.twoFactorAuthentication is TwoFactorAuthentication.Enabled) {
            val challenge = TwoFactorChallenge.issuedFor(user.id)
            twoFactorChallengeRepository.save(challenge)
            SignInOutcome.TwoFactorChallengeRequired(challenge.id)
        } else {
            resetFailedSignInsUseCase.unfold(user.id)
            SignInOutcome.SignedIn(user)
        }
    }

    // Completes a challenged sign-in with the code of the authenticator app
    fun verifyTwoFactorCode(verification: TwoFactorCodeVerification): User.DefinedUser {
        val verifyTwoFactorCodeUseCase = VerifyTwoFactorCodeUseCase(twoFactorChallengeRepository, userRepository)
        try {
            verifyTwoFactorCodeUseCase.unfold(verification)
        } catch (wrongCode: TwoFactorCodeInvalidException) {
            recordFailedSignInUseCase.unfold(challengedEmail(verification.challengeId))
            throw wrongCode
        }
        val user = verifyTwoFactorCodeUseCase.authenticatedUser
        resetFailedSignInsUseCase.unfold(user.id)
        return user
    }

    private fun challengedEmail(challengeId: TwoFactorChallengeId): Email {
        val lookup = twoFactorChallengeRepository.find(challengeId) as TwoFactorChallengeLookup.Found
        return (userRepository.findById(lookup.challenge.userId) as User.DefinedUser).email
    }
}
