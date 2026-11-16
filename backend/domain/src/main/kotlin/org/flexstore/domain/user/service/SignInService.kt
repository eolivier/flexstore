package org.flexstore.domain.user.service

import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.domain.twofactor.model.TwoFactorCodeInvalidException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorChallenge
import org.flexstore.domain.twofactor.model.TwoFactorChallengeLookup
import org.flexstore.domain.twofactor.usecase.TwoFactorCodeVerification
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.GoogleIdentityVerifier
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.usecase.*
import org.ucop.domain.NominalException
import org.ucop.domain.PreConditionException

// Domain service: an account signs in. A wrong password, or a wrong two-factor code, is counted and locks the
// account at the limit (see RecordFailedSignInUseCase); a successful sign-in starts the count again.
class SignInService(
    private val userRepository: UserRepository,
    private val twoFactorChallengeRepository: TwoFactorChallengeRepository,
    private val googleIdentityVerifier: GoogleIdentityVerifier,
    private val signInUseCases: SignInUseCases
) {

    // A user who enabled two-factor authentication is given a challenge instead of a session
    fun signIn(loginRequest: LoginRequest): SignInOutcome {
        val authenticatedUser = try {
            signInUseCases.loginUseCase.unfold(loginRequest)
        } catch (refusal: NominalException) {
            if (refusal.cause is InvalidCredentialsException) {
                signInUseCases.recordFailedSignInUseCase.unfold(loginRequest.email)
            }
            throw refusal
        }
        return outcomeOf(authenticatedUser as User.DefinedUser)
    }

    // A person signs in with their Google account: Google replaces the password, not the second factor
    fun signInWithGoogle(idToken: GoogleIdToken): SignInOutcome {
        val googleIdentity = verified(idToken)
        signInUseCases.signInWithGoogleUseCase.unfold(googleIdentity)
        return outcomeOf(userRepository.findByEmail(googleIdentity.email) as User.DefinedUser)
    }

    // A token that cannot be trusted is refused like a precondition of a use case would refuse it
    private fun verified(idToken: GoogleIdToken): GoogleIdentity =
        try {
            googleIdentityVerifier.verify(idToken)
        } catch (refusal: PreConditionException) {
            throw NominalException(refusal)
        }

    private fun outcomeOf(user: User.DefinedUser): SignInOutcome {
        return if (user.twoFactorAuthentication is TwoFactorAuthentication.Enabled) {
            val challenge = TwoFactorChallenge.issuedFor(user.id)
            twoFactorChallengeRepository.save(challenge)
            SignInOutcome.TwoFactorChallengeRequired(challenge.id)
        } else {
            signInUseCases.resetFailedSignInsUseCase.unfold(user.id)
            SignInOutcome.SignedIn(user)
        }
    }

    // Completes a challenged sign-in with the code of the authenticator app
    fun verifyTwoFactorCode(verification: TwoFactorCodeVerification): User.DefinedUser {
        val lookup = twoFactorChallengeRepository.find(verification.challengeId)
        try {
            signInUseCases.verifyTwoFactorCodeUseCase.unfold(verification)
        } catch (refusal: NominalException) {
            if (refusal.cause is TwoFactorCodeInvalidException) {
                signInUseCases.recordFailedSignInUseCase.unfold(challengedUser(lookup).email)
            }
            throw refusal
        }
        val user = challengedUser(lookup)
        signInUseCases.resetFailedSignInsUseCase.unfold(user.id)
        return user
    }

    // The challenge is found, or the verification would have been refused as unknown or expired
    private fun challengedUser(lookup: TwoFactorChallengeLookup): User.DefinedUser =
        userRepository.findById((lookup as TwoFactorChallengeLookup.Found).challenge.userId) as User.DefinedUser
}
