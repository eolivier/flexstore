package org.flexstore.infra.repository

import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.domain.twofactor.model.TwoFactorChallenge
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorChallengeLookup

// In-memory double of the challenges, for the tests: the application keeps them in the database
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
