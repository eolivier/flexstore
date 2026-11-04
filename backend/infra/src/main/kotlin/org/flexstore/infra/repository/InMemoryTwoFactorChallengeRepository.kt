package org.flexstore.infra.repository

import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.domain.twofactor.model.TwoFactorChallenge
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorChallengeLookup

// Kept in memory: a sign-in challenge is short-lived (see TwoFactorChallenge.VALIDITY) and does not need to
// survive a restart of the application
class InMemoryTwoFactorChallengeRepository : TwoFactorChallengeRepository {

    private val challenges = mutableMapOf<TwoFactorChallengeId, TwoFactorChallenge>()

    override fun save(challenge: TwoFactorChallenge) {
        challenges[challenge.id] = challenge
    }

    override fun find(challengeId: TwoFactorChallengeId): TwoFactorChallengeLookup =
        challenges[challengeId]?.let { TwoFactorChallengeLookup.Found(it) } ?: TwoFactorChallengeLookup.NotFound

    override fun delete(challengeId: TwoFactorChallengeId) {
        challenges.remove(challengeId)
    }
}
