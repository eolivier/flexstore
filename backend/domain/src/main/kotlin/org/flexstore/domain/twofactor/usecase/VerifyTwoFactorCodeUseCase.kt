package org.flexstore.domain.twofactor.usecase

import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.twofactor.TwoFactorChallengeNotFoundException
import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.domain.twofactor.TwoFactorCodeInvalidException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorChallenge
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorChallengeLookup
import org.flexstore.domain.twofactor.model.TwoFactorCode
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.User
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

data class TwoFactorCodeVerification(val challengeId: TwoFactorChallengeId, val code: TwoFactorCode)

// Completes a sign-in that is waiting for its second factor: a wrong code is a failed authentication attempt,
// counted by SignInService exactly like a wrong password (see TwoFactorCodeInvalidException)
class VerifyTwoFactorCodeUseCase(
    private val twoFactorChallengeRepository: TwoFactorChallengeRepository,
    private val userRepository: UserRepository
) : UseCase<TwoFactorCodeVerification> {

    lateinit var authenticatedUser: User.DefinedUser

    override fun getPreConditions(): PreConditions<TwoFactorCodeVerification> = PreConditions(
        challengeNotExpiredCondition(),
        codeMatchesCondition()
    )

    override fun getNominalScenario(): NominalScenario<TwoFactorCodeVerification> =
        NominalScenario(Step { verification ->
            val challenge = challengeOf(verification.challengeId)
            authenticatedUser = userRepository.findById(challenge.userId) as User.DefinedUser
            twoFactorChallengeRepository.delete(challenge.id)
        })

    override fun getPostConditions(): PostConditions<TwoFactorCodeVerification> = PostConditions.none()

    private fun challengeOf(challengeId: TwoFactorChallengeId): TwoFactorChallenge =
        when (val lookup = twoFactorChallengeRepository.find(challengeId)) {
            is TwoFactorChallengeLookup.Found -> lookup.challenge
            TwoFactorChallengeLookup.NotFound -> throw challengeNotFoundException()
        }

    private fun challengeNotFoundException() =
        TwoFactorChallengeNotFoundException(ErrorMessage("The two-factor challenge is unknown or expired."))

    // An expired challenge is dropped as soon as it is found: it cannot be verified again
    private fun challengeNotExpiredCondition() = PreCondition<TwoFactorCodeVerification> { verification ->
        val challenge = challengeOf(verification.challengeId)
        if (challenge.isExpiredAt(InstantProvider.now())) {
            twoFactorChallengeRepository.delete(challenge.id)
            throw challengeNotFoundException()
        }
    }

    private fun codeMatchesCondition() = PreCondition<TwoFactorCodeVerification> { verification ->
        val challenge = challengeOf(verification.challengeId)
        val user = userRepository.findById(challenge.userId) as User.DefinedUser
        val enabled = user.twoFactorAuthentication as TwoFactorAuthentication.Enabled
        if (enabled.secret.doesNotMatch(verification.code, InstantProvider.now())) {
            throw TwoFactorCodeInvalidException(ErrorMessage("The two-factor code is wrong."))
        }
    }
}
