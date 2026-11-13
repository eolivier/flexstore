package org.flexstore.domain.user.service

import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.domain.twofactor.model.TwoFactorCodeInvalidException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorChallenge
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorChallengeLookup
import org.flexstore.domain.twofactor.usecase.TwoFactorCodeVerification
import org.flexstore.domain.twofactor.usecase.VerifyTwoFactorCodeUseCase
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.GoogleIdentityVerifier
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.usecase.*
import org.ucop.domain.NominalException

// Domain service: an account signs in. A wrong password, or a wrong two-factor code, is counted and locks the
// account at the limit (see RecordFailedSignInUseCase); a successful sign-in starts the count again. The user
// repository only builds the login and the two-factor verification use cases, which keep the user they
// authenticated: one per sign-in, as they are not shared between calls.
class SignInService(
    private val userRepository: UserRepository,
    private val recordFailedSignInUseCase: RecordFailedSignInUseCase,
    private val resetFailedSignInsUseCase: ResetFailedSignInsUseCase,
    private val twoFactorChallengeRepository: TwoFactorChallengeRepository,
    private val googleIdentityVerifier: GoogleIdentityVerifier
) {

    // A user who enabled two-factor authentication is given a challenge instead of a session
    fun signIn(loginRequest: LoginRequest): SignInOutcome {
        val loginUseCase = LoginUseCase(userRepository)
        try {
            loginUseCase.unfold(loginRequest)
        } catch (refusal: NominalException) {
            if (refusal.cause is InvalidCredentialsException) {
                recordFailedSignInUseCase.unfold(loginRequest.email)
            }
            throw refusal
        }
        return outcomeOf(loginUseCase.authenticatedUser as User.DefinedUser)
    }

    // A person signs in with their Google account: Google replaces the password, not the second factor
    fun signInWithGoogle(idToken: GoogleIdToken): SignInOutcome {
        val signInWithGoogleUseCase = SignInWithGoogleUseCase(userRepository, googleIdentityVerifier)
        signInWithGoogleUseCase.unfold(idToken)
        return outcomeOf(signInWithGoogleUseCase.authenticatedUser as User.DefinedUser)
    }

    private fun outcomeOf(user: User.DefinedUser): SignInOutcome {
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
        } catch (refusal: NominalException) {
            if (refusal.cause is TwoFactorCodeInvalidException) {
                recordFailedSignInUseCase.unfold(challengedEmail(verification.challengeId))
            }
            throw refusal
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
